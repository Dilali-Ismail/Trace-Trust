package org.usermanagement.traceandtrust.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseDto> createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest request) {

        WarehouseDto createdWarehouse = warehouseService.createWarehouse(request);
        return new ResponseEntity<>(createdWarehouse, HttpStatus.CREATED);
    }
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WarehouseDto>> getAllWarehouses(
           ) {

        List<WarehouseDto> warehouses = warehouseService.getAllWarehouses();
        return ResponseEntity.ok(warehouses);
    }
    @GetMapping("/{warehouseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WarehouseDto> getWarehouseById(
            @PathVariable UUID warehouseId) {

        WarehouseDto warehouse = warehouseService.getWarehouseById(warehouseId);
        return ResponseEntity.ok(warehouse);
    }
    @PutMapping("/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseDto> updateWarehouse(
            @PathVariable UUID warehouseId,
            @Valid @RequestBody UpdateWarehouseRequest request) {

        WarehouseDto updatedWarehouse = warehouseService.updateWarehouse(warehouseId, request);
        return ResponseEntity.ok(updatedWarehouse);

    }
    @DeleteMapping("/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWarehouse(
            @PathVariable UUID warehouseId) {

        warehouseService.deleteWarehouse(warehouseId);
        return ResponseEntity.noContent().build();
    }

    }
