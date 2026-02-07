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
            @PathVariable UUID orderId,
            @RequestParam UUID warehouseId
    ){
        SalesOrderDto reservedOrder = salesOrderService.reserveOrder(orderId, warehouseId);
        return ResponseEntity.ok(reservedOrder);
    }

    @PostMapping("/{orderId}/request-reservation")
    @PreAuthorize("hasRole('CLIENT') and @ownershipService.isOrderOwner(#orderId, authentication.name)")
    public ResponseEntity<SalesOrderDto> requestReservation(
            @PathVariable UUID orderId,
            @RequestParam UUID warehouseId
    ){
        SalesOrderDto reservedOrder = salesOrderService.requestReservation(orderId, warehouseId);
        return ResponseEntity.ok(reservedOrder);
    }


    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @ownershipService.isOrderOwner(#orderId, authentication.name))")
    public ResponseEntity<SalesOrderDto> cancelOrder(
            @PathVariable UUID orderId,
            @RequestParam(required = false) UUID warehouseId) {

        SalesOrderDto canceledOrder = salesOrderService.cancelOrder(orderId, warehouseId);
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
