package org.usermanagement.traceandtrust.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CreateSupplierRequest;
import org.usermanagement.traceandtrust.dto.SupplierDto;
import org.usermanagement.traceandtrust.service.SupplierService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierDto> createSupplier(
            @RequestHeader("X-Actor-ID") UUID actorId,
            @Valid @RequestBody CreateSupplierRequest request) {
        SupplierDto createdSupplier = supplierService.createSupplier(request, actorId);
        return new ResponseEntity<>(createdSupplier, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SupplierDto>> getAllSuppliers(@RequestHeader("X-Actor-ID") UUID actorId) {
        List<SupplierDto> suppliers = supplierService.getAllSuppliers(actorId);
        return ResponseEntity.ok(suppliers);
    }


}
