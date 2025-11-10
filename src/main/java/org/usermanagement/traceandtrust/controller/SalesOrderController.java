package org.usermanagement.traceandtrust.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CreateSalesOrderRequest;
import org.usermanagement.traceandtrust.dto.SalesOrderDto;
import org.usermanagement.traceandtrust.service.SalesOrderService;

import java.util.UUID;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping
    public ResponseEntity<SalesOrderDto> createSalesOrder(
            @RequestHeader("X-Actor-ID") UUID actorId,
            @Valid @RequestBody CreateSalesOrderRequest request) {

        SalesOrderDto createdOrder = salesOrderService.createSalesOrder(request, actorId);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

}
