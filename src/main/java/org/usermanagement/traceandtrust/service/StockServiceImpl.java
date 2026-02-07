package org.usermanagement.traceandtrust.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.CreateMovementRequest;
import org.usermanagement.traceandtrust.dto.InventoryDto;
import org.usermanagement.traceandtrust.dto.InventoryMovementDto;
import org.usermanagement.traceandtrust.dto.ReservationResult;
import org.usermanagement.traceandtrust.entity.*;
import org.usermanagement.traceandtrust.enums.MovementType;
import org.usermanagement.traceandtrust.exception.BusinessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.InventoryMapper;
import org.usermanagement.traceandtrust.repository.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovement;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public InventoryDto recordMovement(CreateMovementRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + request.getWarehouseId()));

        Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse)
                .orElseGet(() -> Inventory.builder()
                        .product(product)
                        .warehouse(warehouse)
                        .quantity_hand(0L)
                        .quantity_reserved(0L)
                        .build());

        switch (request.getType()) {
            case INBOUND -> inventory.setQuantity_hand(inventory.getQuantity_hand() + request.getQuantity());
            case OUTBOUND -> {
                long availableStock = inventory.getQuantity_hand() - inventory.getQuantity_reserved();
                if (availableStock < request.getQuantity()) {
                    throw new BusinessException("Insufficient stock for outbound movement. Available: " + availableStock + ", Requested: " + request.getQuantity());
                }
                inventory.setQuantity_hand(inventory.getQuantity_hand() - request.getQuantity());
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

    @Override
    @Transactional
    public ReservationResult reserveStock(List<SalesOrderLine> orderLines, UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));

        java.util.Map<UUID, Integer> reservedQuantities = new java.util.HashMap<>();
        java.util.Map<UUID, Integer> backorderQuantities = new java.util.HashMap<>();
        boolean fullyReserved = true;

        for (SalesOrderLine line : orderLines) {
            Product product = line.getProduct();
            int requiredQty = line.getQuantity();

            Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse)
                    .orElseGet(() -> {
                        Inventory newInv = Inventory.builder()
                                .product(product)
                                .warehouse(warehouse)
                                .quantity_hand(0L)
                                .quantity_reserved(0L)
                                .build();
                        return inventoryRepository.save(newInv);
                    });

            long availableStock = inventory.getQuantity_hand() - inventory.getQuantity_reserved();
            int reserveQty = (int) Math.min(requiredQty, availableStock);
            int backorderQty = requiredQty - reserveQty;

            if (reserveQty > 0) {
                inventory.setQuantity_reserved(inventory.getQuantity_reserved() + reserveQty);
                inventoryRepository.save(inventory);
                reservedQuantities.put(product.getId(), reserveQty);
            }

            if (backorderQty > 0) {
                backorderQuantities.put(product.getId(), backorderQty);
                fullyReserved = false;
            }
        }

        return ReservationResult.builder()
                .fullyReserved(fullyReserved)
                .reservedQuantities(reservedQuantities)
                .backorderQuantities(backorderQuantities)
                .build();
    }

    @Override
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

    @Override
    @Transactional
    public void dispatchStock(List<SalesOrderLine> orderLines, UUID warehouseId) {
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
                    .referenceDocument("Sales Order Shipment")
                    .build();
            inventoryMovement.save(movement);
        }
    }

    @Override
    public List<InventoryDto> getStock(UUID warehouseId, UUID productId) {
        if (warehouseId != null && productId != null) {
            Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
            return inventoryRepository.findByProductAndWarehouse(product, warehouse)
                    .stream().map(inventoryMapper::toDto).collect(Collectors.toList());
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
