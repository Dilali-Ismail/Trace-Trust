package org.usermanagement.traceandtrust.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CreateSalesOrderRequest;
import org.usermanagement.traceandtrust.dto.SalesOrderDto;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.service.SalesOrderService;
import org.usermanagement.traceandtrust.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<SalesOrderDto> createSalesOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateSalesOrderRequest request) {
        User user = userService.syncUser(jwt);
        SalesOrderDto createdOrder = salesOrderService.createSalesOrder(request, user.getId());
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PostMapping("/{orderId}/reserve")
    public ResponseEntity<SalesOrderDto> reserveOrder(
             @AuthenticationPrincipal Jwt jwt,
             @PathVariable UUID orderId
    ){
         User user = userService.syncUser(jwt);
        SalesOrderDto reservedOrder = salesOrderService.reserveOrder(orderId, user.getId());
        return ResponseEntity.ok(reservedOrder);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<SalesOrderDto> cancelOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId) {

        User user = userService.syncUser(jwt);
        SalesOrderDto canceledOrder = salesOrderService.cancelOrder(orderId, user.getId());
        return ResponseEntity.ok(canceledOrder);
    }
}
