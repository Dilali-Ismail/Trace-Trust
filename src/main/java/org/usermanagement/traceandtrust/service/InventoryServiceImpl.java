package org.usermanagement.traceandtrust.service;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.stat.CacheableDataStatistics;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.CreateMovementRequest;
import org.usermanagement.traceandtrust.dto.InventoryDto;
import org.usermanagement.traceandtrust.entity.*;
import org.usermanagement.traceandtrust.enums.MovementType;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.BusinessException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.exception.StockUnavailableException;
import org.usermanagement.traceandtrust.mapper.InventoryMapper;
import org.usermanagement.traceandtrust.repository.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.usermanagement.traceandtrust.dto.InventoryMovementDto;
@Service
@RequiredArgsConstructor

public class InventoryServiceImpl implements InventoryService{
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovement;
    private final InventoryMapper inventoryMapper;

    @Transactional
   public InventoryDto recordMovement(CreateMovementRequest request){
       Product product = productRepository.findById(request.getProductId())
               .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
       Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
               .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + request.getWarehouseId()));

        Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse)
                .orElseGet(()-> Inventory.builder()
                        .product(product)
                        .warehouse(warehouse)
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

    public void reserveStock(List<SalesOrderLine> orderLines, UUID warehouseId) {

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));
        for (SalesOrderLine line : orderLines) {
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

            inventory.setQuantity_reserved(inventory.getQuantity_reserved() + quantityToReserve);
            inventoryRepository.save(inventory);
        }
    }
    @Transactional
    public void releaseStock(List<SalesOrderLine> orderLines, UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));
        for (SalesOrderLine line : orderLines) {
            Product product = line.getProduct();
            int quantityToRelease = line.getQuantity();
            inventoryRepository.findByProductAndWarehouse(product, warehouse).ifPresent(inventory -> {
                long newReservedQty = inventory.getQuantity_reserved() - quantityToRelease;
                inventory.setQuantity_reserved(Math.max(0, newReservedQty));
                inventoryRepository.save(inventory);
            });
        }
    }
    public void dispatchStock(List<SalesOrderLine> orderLines, UUID warehouseId){

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));
        for (SalesOrderLine line : orderLines) {
            Product product = line.getProduct();
            int quantityToDispatch = line.getQuantity();

            Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse)
                    .orElseThrow(() -> new BusinessException("Cannot dispatch stock: Inventory record not found for product " + product.getSku()));

            if (inventory.getQuantity_hand() < quantityToDispatch || inventory.getQuantity_reserved() < quantityToDispatch) {
                throw new BusinessException("Cannot dispatch stock for product " + product.getSku() + ": Inconsistent stock levels.");
            }

            inventory.setQuantity_hand(inventory.getQuantity_hand() - quantityToDispatch);
            inventory.setQuantity_reserved(inventory.getQuantity_reserved() - quantityToDispatch);
            inventoryRepository.save(inventory);

            InventoryMovement movement = InventoryMovement.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .type(MovementType.OUTBOUND)
                    .quantity(quantityToDispatch)
                    .referenceDocument("Outbonding")
                    .build();


            inventoryMovement.save(movement);
        }
    }

    @Override
    public List<InventoryDto> getStock(UUID warehouseId, UUID productId) {
        if (warehouseId != null && productId != null) {
            return inventoryRepository.findByProductAndWarehouse(
                    productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found")),
                    warehouseRepository.findById(warehouseId).orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"))
            ).stream().map(inventoryMapper::toDto).collect(Collectors.toList());
        } else if (warehouseId != null) {
            return inventoryRepository.findByWarehouseId(warehouseId).stream()
                    .map(inventoryMapper::toDto)
                    .collect(Collectors.toList());
        } else if (productId != null) {
             return inventoryRepository.findByProductId(productId).stream()
                    .map(inventoryMapper::toDto)
                    .collect(Collectors.toList());
        } else {
            return inventoryRepository.findAll().stream()
                    .map(inventoryMapper::toDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<InventoryMovementDto> getHistory(UUID warehouseId, UUID productId) {
        List<InventoryMovement> movements;
        if (warehouseId != null && productId != null) {
            movements = inventoryMovement.findByWarehouseIdAndProductId(warehouseId, productId);
        } else if (warehouseId != null) {
            movements = inventoryMovement.findByWarehouseId(warehouseId);
        } else if (productId != null) {
            movements = inventoryMovement.findByProductId(productId);
        } else {
            movements = inventoryMovement.findAll();
        }

        return movements.stream()
            .map(this::toMovementDto)
            .collect(Collectors.toList());
    }

    private InventoryMovementDto toMovementDto(InventoryMovement movement) {
        return InventoryMovementDto.builder()
            .id(movement.getId())
            .productId(movement.getProduct().getId())
            .productSku(movement.getProduct().getSku())
            .warehouseId(movement.getWarehouse().getId())
            .warehouseName(movement.getWarehouse().getName())
            .type(movement.getType())
            .quantity(movement.getQuantity())
            .referenceDocument(movement.getReferenceDocument())
            .occurredAt(movement.getOccurredAt())
            .build();
    }
}
