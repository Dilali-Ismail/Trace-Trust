package org.usermanagement.traceandtrust.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CarrierDto;
import org.usermanagement.traceandtrust.dto.CreateCarrierRequest;
import org.usermanagement.traceandtrust.service.CarrierService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/carriers")
@RequiredArgsConstructor
public class CarrierController {
    private final CarrierService carrierService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CarrierDto> createCarrier( @Valid @RequestBody CreateCarrierRequest request) {
        return new ResponseEntity<>(carrierService.createCarrier(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<List<CarrierDto>> getAllCarriers() {
        return ResponseEntity.ok(carrierService.getAllCarriers());
    }

}
