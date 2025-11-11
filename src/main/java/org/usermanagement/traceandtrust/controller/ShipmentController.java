package org.usermanagement.traceandtrust.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CreateShipmentRequest;
import org.usermanagement.traceandtrust.dto.ShipmentDto;
import org.usermanagement.traceandtrust.service.ShipmentService;

import java.util.UUID;


@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {
    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentDto> createShipment(
            @RequestHeader("X-Actor-ID") UUID actorId,
            @Valid @RequestBody CreateShipmentRequest request) {

        ShipmentDto createdShipment = shipmentService.createShipment(request, actorId);
        return new ResponseEntity<>(createdShipment, HttpStatus.CREATED);
    }




}
