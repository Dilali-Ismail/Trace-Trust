package org.usermanagement.traceandtrust.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CreatePurchaseOrderRequest;
import org.usermanagement.traceandtrust.dto.PurchaseOrderDto;
import org.usermanagement.traceandtrust.dto.ReceivePurchaseOrderRequest;
import org.usermanagement.traceandtrust.service.PurchaseOrderService;

import java.util.UUID;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PurchaseOrderDto> createPurchOrder(
           @Valid @RequestBody CreatePurchaseOrderRequest request
            ){
       PurchaseOrderDto purchaseOrderDto = purchaseOrderService.createPurshOrder(request);

       return new ResponseEntity<>(purchaseOrderDto, HttpStatus.CREATED);
    }

    @PostMapping("/{purchaseOrderId}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<PurchaseOrderDto> receiveItems(
            @PathVariable UUID purchaseOrderId,
            @Valid @RequestBody ReceivePurchaseOrderRequest request) {

        PurchaseOrderDto updatedOrder = purchaseOrderService.receivePurchaseOrderItems(purchaseOrderId, request);
        return ResponseEntity.ok(updatedOrder);
    }
}
