package org.usermanagement.traceandtrust.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CreateSalesOrderRequest;
import org.usermanagement.traceandtrust.dto.SalesOrderDto;
import org.usermanagement.traceandtrust.service.SalesOrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<SalesOrderDto> createSalesOrder(
            @Valid @RequestBody CreateSalesOrderRequest request) {

        SalesOrderDto createdOrder = salesOrderService.createSalesOrder(request);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }
    @PostMapping("/{orderId}/reserve")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<SalesOrderDto>reserveOrder(
            @PathVariable UUID orderId
    ){
        SalesOrderDto reservedOrder = salesOrderService.reserveOrder(orderId);
        return ResponseEntity.ok(reservedOrder);
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @ownershipService.isOrderOwner(#orderId, authentication.name))")
    public ResponseEntity<SalesOrderDto> cancelOrder(
            @PathVariable UUID orderId) {

        SalesOrderDto canceledOrder = salesOrderService.cancelOrder(orderId);
        return ResponseEntity.ok(canceledOrder);
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CLIENT')")
    public ResponseEntity<List<SalesOrderDto>> getAllSalesOrders() {
        return ResponseEntity.ok(salesOrderService.getAllSalesOrders());
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CLIENT')")
    public ResponseEntity<SalesOrderDto> getSalesOrderById(@PathVariable UUID orderId) {
        return ResponseEntity.ok(salesOrderService.getSalesOrderById(orderId));
    }
}
