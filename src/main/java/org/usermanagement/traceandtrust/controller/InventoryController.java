package org.usermanagement.traceandtrust.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CreateMovementRequest;
import org.usermanagement.traceandtrust.dto.InventoryDto;
import org.usermanagement.traceandtrust.dto.InventoryMovementDto;
import org.usermanagement.traceandtrust.service.StockService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final StockService inventoryService;

    @PostMapping("/movements")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<InventoryDto> recordMovement(
            @Valid @RequestBody CreateMovementRequest request) {
        InventoryDto updatedInventory = inventoryService.recordMovement(request);
        return ResponseEntity.ok(updatedInventory);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'ADMIN')")
    public ResponseEntity<List<InventoryDto>> getInventory(
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) UUID productId) {
        return ResponseEntity.ok(inventoryService.getStock(warehouseId, productId));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'ADMIN')")
    public ResponseEntity<List<InventoryMovementDto>> getHistory(
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) UUID productId) {
        return ResponseEntity.ok(inventoryService.getHistory(warehouseId, productId));
    }
}
