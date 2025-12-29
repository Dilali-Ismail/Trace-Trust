package org.usermanagement.traceandtrust.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CreateMovementRequest;
import org.usermanagement.traceandtrust.dto.InventoryDto;
import org.usermanagement.traceandtrust.service.InventoryService;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/movements")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<InventoryDto> recordMovement(
            @Valid @RequestBody CreateMovementRequest request) {

        InventoryDto updatedInventory = inventoryService.recordMovement(request);
        return ResponseEntity.ok(updatedInventory);
    }
}
