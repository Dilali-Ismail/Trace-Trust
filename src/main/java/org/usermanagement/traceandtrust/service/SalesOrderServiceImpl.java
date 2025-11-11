package org.usermanagement.traceandtrust.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.CreateSalesOrderRequest;
import org.usermanagement.traceandtrust.dto.SalesOrderDto;
import org.usermanagement.traceandtrust.dto.SalesOrderLineDto;
import org.usermanagement.traceandtrust.entity.*;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.enums.SalesOrderStatus;
import org.usermanagement.traceandtrust.exception.BusinessException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.SalesOrderMapper;
import org.usermanagement.traceandtrust.repository.ProductRepository;
import org.usermanagement.traceandtrust.repository.SalesOrderRepository;
import org.usermanagement.traceandtrust.repository.UserRepository;
import org.usermanagement.traceandtrust.repository.WarehouseRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {
    private final SalesOrderRepository salesOrderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final InventoryService inventoryService;

    public SalesOrderDto createSalesOrder(CreateSalesOrderRequest request, UUID actorId){

        User client = checkClientRole(actorId);

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + request.getWarehouseId()));

        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setClient(client);
        salesOrder.setWarehouse(warehouse);
        salesOrder.setStatus(SalesOrderStatus.CREATED);

        for (SalesOrderLineDto lineDto : request.getOrderLines()) {

            Product product = productRepository.findById(lineDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + lineDto.getProductId()));
            if (!product.isActive()) {
                throw new BusinessException("Product with SKU " + product.getSku() + " is not available for sale.");
            }

            SalesOrderLine line = new SalesOrderLine();
            line.setProduct(product);
            line.setQuantity(lineDto.getQuantity());
            line.setUnitPrice(lineDto.getUnitPrice());

            salesOrder.addLine(line);

        }
        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);
        return salesOrderMapper.toDto(savedOrder);

    }

    public SalesOrderDto reserveOrder(UUID orderId, UUID actorId){
        SalesOrder salesOrder = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with id: " + orderId));

        if (salesOrder.getStatus() != SalesOrderStatus.CREATED) {
            throw new BusinessException("Only orders with CREATED status can be reserved. Current status: " + salesOrder.getStatus());
        }

        inventoryService.reserveStock(salesOrder.getOrderLines(),salesOrder.getWarehouse().getId(),actorId);
        salesOrder.setStatus(SalesOrderStatus.RESERVED);
        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);
        return salesOrderMapper.toDto(savedOrder);

    }

    @Override
    @Transactional
    public SalesOrderDto cancelOrder(UUID orderId, UUID actorId){
        checkAdminRole(actorId);

        SalesOrder salesOrder = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with id: " + orderId));
        switch (salesOrder.getStatus()) {
            case CREATED:
                salesOrder.setStatus(SalesOrderStatus.CANCELED);
                break;

            case RESERVED:

                inventoryService.releaseStock(salesOrder.getOrderLines(), salesOrder.getWarehouse().getId(), actorId);
                salesOrder.setStatus(SalesOrderStatus.CANCELED);
                break;

            case SHIPPED:
            case DELIVERED:
                throw new BusinessException("Cannot cancel an order that has already been shipped or delivered.");

            case CANCELED:

                throw new BusinessException("This order has already been canceled.");
        }

        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);
        return salesOrderMapper.toDto(savedOrder);

    }

    private User checkClientRole(UUID actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with id: " + actorId));
        if (actor.getRole() != Role.CLIENT) {
            throw new ForbiddenAccessException("Only CLIENT users can create sales orders.");
        }
        return actor;
    }
    private void checkAdminRole(UUID actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with id: " + actorId));
        if (actor.getRole() != Role.ADMIN) {
            throw new ForbiddenAccessException("This operation is restricted to ADMIN users.");
        }
    }

}

