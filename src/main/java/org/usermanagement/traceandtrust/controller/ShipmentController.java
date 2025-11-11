package org.usermanagement.traceandtrust.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CreateShipmentRequest;
import org.usermanagement.traceandtrust.dto.ShipmentDto;
import org.usermanagement.traceandtrust.dto.SupplierDto;
import org.usermanagement.traceandtrust.service.ShipmentService;

import java.util.List;
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

    @GetMapping
    public ResponseEntity<List<ShipmentDto>> getAllShipments(@RequestHeader("X-Actor-ID") UUID actorId) {
        List<ShipmentDto> shipments = shipmentService.getAllShipments(actorId);
        return ResponseEntity.ok(shipments);
    }

    @PatchMapping("/{shipmentId}/dispatch")
    public ResponseEntity<ShipmentDto> dispatchShipment(
            @PathVariable UUID shipmentId,
            @RequestHeader("X-Actor-ID") UUID actorId) {

        ShipmentDto dispatchedShipment = shipmentService.dispatchShipment(shipmentId, actorId);
        return ResponseEntity.ok(dispatchedShipment);
    }

    @PatchMapping("/{shipmentId}/deliver")
    public ResponseEntity<ShipmentDto> markAsDelivered(
            @PathVariable UUID shipmentId,
            @RequestHeader("X-Actor-ID") UUID actorId) {

        ShipmentDto deliveredShipment = shipmentService.markShipmentAsDelivered(shipmentId, actorId);
        return ResponseEntity.ok(deliveredShipment);
    }




}
