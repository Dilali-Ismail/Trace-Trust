package org.usermanagement.traceandtrust.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ShipmentDto> createShipment(
            @Valid @RequestBody CreateShipmentRequest request) {

        ShipmentDto createdShipment = shipmentService.createShipment(request);
        return new ResponseEntity<>(createdShipment, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ShipmentDto>> getAllShipments() {
        List<ShipmentDto> shipments = shipmentService.getAllShipments();
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/{shipmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShipmentDto> getShipmentById(@PathVariable UUID shipmentId) {
        ShipmentDto shipment = shipmentService.getShipmentById(shipmentId);
        return ResponseEntity.ok(shipment);
    }

    @PostMapping("/{shipmentId}/dispatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ShipmentDto> dispatchShipment(
            @PathVariable UUID shipmentId,
            @RequestParam UUID warehouseId) {
        ShipmentDto dispatchedShipment = shipmentService.dispatchShipment(shipmentId, warehouseId);
        return ResponseEntity.ok(dispatchedShipment);
    }

    @PatchMapping("/{shipmentId}/deliver")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ShipmentDto> markAsDelivered(
            @PathVariable UUID shipmentId) {

        ShipmentDto deliveredShipment = shipmentService.markShipmentAsDelivered(shipmentId);
        return ResponseEntity.ok(deliveredShipment);
    }




}
