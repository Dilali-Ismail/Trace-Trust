package org.usermanagement.traceandtrust.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.CreateSalesOrderRequest;
import org.springframework.security.core.Authentication;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderServiceImpl implements SalesOrderService {
    private final SalesOrderRepository salesOrderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final StockService inventoryService;

    public SalesOrderDto createSalesOrder(CreateSalesOrderRequest request){
        String email = getCurrentUserEmail();

        User client = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with email: " + email)
                );

        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setClient(client);
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

        MDC.put("business_id", savedOrder.getId().toString());
        log.info("Sales order created successfully");
        MDC.remove("business_id");

        return salesOrderMapper.toDto(savedOrder);

    }

    @Transactional
    public SalesOrderDto reserveOrder(UUID orderId, UUID warehouseId) {
        SalesOrder salesOrder = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with id: " + orderId));

        if (salesOrder.getStatus() != SalesOrderStatus.CREATED) {
            throw new BusinessException("Only orders with CREATED status can be reserved. Current status: " + salesOrder.getStatus());
        }

        org.usermanagement.traceandtrust.dto.ReservationResult result = inventoryService.reserveStock(salesOrder.getOrderLines(), warehouseId);

        // Update lines with backorders if any
        for (SalesOrderLine line : salesOrder.getOrderLines()) {
            Integer backorderQty = result.getBackorderQuantities().get(line.getProduct().getId());
            if (backorderQty != null && backorderQty > 0) {
                SalesOrderBackorder backorder = SalesOrderBackorder.builder()
                        .salesOrderLine(line)
                        .quantityPending(backorderQty)
                        .reason("Insufficient stock in warehouse " + warehouseId)
                        .build();
                line.setBackorder(backorder);
            }
        }

        if (result.isFullyReserved()) {
            salesOrder.setStatus(SalesOrderStatus.RESERVED);
        } else {
            // If partial, we still mark as RESERVED (or maybe PARTIALLY_RESERVED if we had that enum)
            // The requirement says "Action “Réserver” : affiche backorders si partiel"
            // Let's keep RESERVED for now, but the presence of backorders in DTO will show the partial state
            salesOrder.setStatus(SalesOrderStatus.RESERVED);
        }

        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);

        MDC.put("business_id", orderId.toString());
        log.info("Sales order reservation processed. Fully reserved: {}", result.isFullyReserved());
        MDC.remove("business_id");

        return salesOrderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public SalesOrderDto requestReservation(UUID orderId, UUID warehouseId) {
        // Here we can enforce specific business rules for CLIENTS
        // Currently, it's the same logic as admin reserveOrder, but could differ later
        return reserveOrder(orderId, warehouseId);
    }


    @Override
    @Transactional
    public SalesOrderDto cancelOrder(UUID orderId, UUID warehouseId){

        SalesOrder salesOrder = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with id: " + orderId));
        switch (salesOrder.getStatus()) {
            case CREATED:
                salesOrder.setStatus(SalesOrderStatus.CANCELED);
                break;

            case RESERVED:
                if (warehouseId == null) {
                    throw new BusinessException("Warehouse ID is required to release stock for reserved order cancellation.");
                }
                inventoryService.releaseStock(salesOrder.getOrderLines(), warehouseId);
                salesOrder.setStatus(SalesOrderStatus.CANCELED);
                break;

            case SHIPPED:
            case DELIVERED:
                throw new BusinessException("Cannot cancel an order that has already been shipped or delivered.");

            case CANCELED:
                throw new BusinessException("This order has already been canceled.");
        }

        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);
        MDC.put("business_id", orderId.toString());
        log.info("Sales order canceled. New status: {}", savedOrder.getStatus());
        MDC.remove("business_id");

        return salesOrderMapper.toDto(savedOrder);

    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        // Retourne l'email (c'est le "username" dans notre UserDetailsService)
        return authentication.getName();
    }

   public  List<SalesOrderDto> getAllSalesOrders(){
       String role = getCurrentUserRole();
       String email = getCurrentUserEmail();
       if ("ROLE_CLIENT".equals(role)) {
           return salesOrderRepository.findAllByClientEmail(email).stream()
                   .map(salesOrderMapper::toDto)
                   .collect(Collectors.toList());
       }
       return salesOrderRepository.findAll().stream()
               .map(salesOrderMapper::toDto)
               .collect(Collectors.toList());
   }

    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().iterator().next().getAuthority();
    }

    @Override
    public SalesOrderDto getSalesOrderById(UUID id) {
        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with id: " + id));

        String role = getCurrentUserRole();
        String email = getCurrentUserEmail();

        if ("ROLE_CLIENT".equals(role) && !salesOrder.getClient().getEmail().equals(email)) {
             throw new ForbiddenAccessException("You are not authorized to view this order.");
        }

        return salesOrderMapper.toDto(salesOrder);
    }
}

