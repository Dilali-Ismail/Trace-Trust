package org.usermanagement.traceandtrust.service;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.stat.CacheableDataStatistics;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.CreateMovementRequest;
import org.usermanagement.traceandtrust.dto.InventoryDto;
import org.usermanagement.traceandtrust.entity.*;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.BusinessException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.exception.StockUnavailableException;
import org.usermanagement.traceandtrust.mapper.InventoryMapper;
import org.usermanagement.traceandtrust.repository.*;

import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor

public class InventoryServiceImpl implements InventoryService{
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovement;
    private final InventoryMapper inventoryMapper;

    @Transactional
   public InventoryDto recordMovement(CreateMovementRequest request, UUID actorId){
         checkAdminRole(actorId);
       Product product = productRepository.findById(request.getProductId())
               .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
       Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
               .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + request.getWarehouseId()));

        Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse)
                .orElseGet(()-> Inventory.builder()
                        .product(product)
                        .warehouse(warehouse)
                        .quantity_hand(request.getQuantity())
                        .build());

        switch(request.getType()){
            case INBOUND ->{
                inventory.setQuantity_hand(inventory.getQuantity_hand() + request.getQuantity());
                break;
            }
            case ADJUSTMENT -> {
                long newQtyOnHand = request.getQuantity();

                if (newQtyOnHand < 0) {
                    throw new BusinessException("Adjustment quantity cannot be a negative value.");
                }

                if (newQtyOnHand < inventory.getQuantity_reserved()) {
                    throw new BusinessException("Adjustment failed: new quantity on hand cannot be less than reserved quantity.");
                }
                inventory.setQuantity_hand(newQtyOnHand);
                break;
            }
        }
        Inventory savedInventory = inventoryRepository.save(inventory);

        InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .warehouse(warehouse)
                .type(request.getType())
                .quantity(request.getQuantity())
                .referenceDocument(request.getReferenceDocument())
                .build();
         inventoryMovement.save(movement);
         return inventoryMapper.toDto(savedInventory);
   }
    public void reserveStock(List<SalesOrderLine> orderLines, UUID warehouseId, UUID actorId){
        checkAdminRole(actorId);

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));

        for (SalesOrderLine line : orderLines){

            Product product = line.getProduct();
            int quantityToReserve = line.getQuantity();

            Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse)
                    .orElseThrow(() -> new StockUnavailableException("No stock available for product " + product.getSku()));

            long availableStock = inventory.getQuantity_hand() - inventory.getQuantity_reserved();

            if (availableStock < quantityToReserve) {
                throw new StockUnavailableException(
                        "Insufficient stock for product " + product.getSku() +
                                ". Required: " + quantityToReserve + ", Available: " + availableStock
                );
            }

            inventory.setQuantity_reserved(inventory.getQuantity_reserved() + quantityToReserve );
            inventoryRepository.save(inventory);
        }
    }
    @Transactional
    public void releaseStock(List<SalesOrderLine> orderLines, UUID warehouseId, UUID actorId){


    }
    private void checkAdminRole(UUID actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user with ID " + actorId + " not found."));

        if (actor.getRole() != Role.WAREHOUSE_MANAGER ) {
            throw new ForbiddenAccessException("Only WAREHOUSE_MANAGER can record stock movements..");
        }
    }
}
