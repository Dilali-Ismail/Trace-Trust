package org.usermanagement.traceandtrust.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CreateWarehouseRequest;
import org.usermanagement.traceandtrust.dto.UpdateWarehouseRequest;
import org.usermanagement.traceandtrust.dto.WarehouseDto;
import org.usermanagement.traceandtrust.service.WarehouseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseService warehouseService;
    @PostMapping
    public ResponseEntity<WarehouseDto> createWarehouse(
            @RequestHeader("X-Actor-ID") UUID actorId,
            @Valid @RequestBody CreateWarehouseRequest request) {

        WarehouseDto createdWarehouse = warehouseService.createWarehouse(request, actorId);
        return new ResponseEntity<>(createdWarehouse, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<WarehouseDto>> getAllWarehouses(
            @RequestHeader("X-Actor-ID") UUID actorId) {

        List<WarehouseDto> warehouses = warehouseService.getAllWarehouses(actorId);
        return ResponseEntity.ok(warehouses);
    }
    @GetMapping("/{warehouseId}")
    public ResponseEntity<WarehouseDto> getWarehouseById(
            @PathVariable UUID warehouseId,
            @RequestHeader("X-Actor-ID") UUID actorId) {

        WarehouseDto warehouse = warehouseService.getWarehouseById(warehouseId, actorId);
        return ResponseEntity.ok(warehouse);
    }
    @PutMapping("/{warehouseId}")
    public ResponseEntity<WarehouseDto> updateWarehouse(
            @PathVariable UUID warehouseId,
            @RequestHeader("X-Actor-ID") UUID actorId,
            @Valid @RequestBody UpdateWarehouseRequest request) {

        WarehouseDto updatedWarehouse = warehouseService.updateWarehouse(warehouseId, request, actorId);
        return ResponseEntity.ok(updatedWarehouse);

    }
    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<Void> deleteWarehouse(
            @PathVariable UUID warehouseId,
            @RequestHeader("X-Actor-ID") UUID actorId) {

        warehouseService.deleteWarehouse(warehouseId, actorId);
        return ResponseEntity.noContent().build();
    }

    }
