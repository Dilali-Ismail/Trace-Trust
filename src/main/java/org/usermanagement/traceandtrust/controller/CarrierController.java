package org.usermanagement.traceandtrust.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<CarrierDto> createCarrier(@RequestHeader("X-Actor-ID") UUID actorId, @Valid @RequestBody CreateCarrierRequest request) {
        return new ResponseEntity<>(carrierService.createCarrier(request, actorId), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CarrierDto>> getAllCarriers(@RequestHeader("X-Actor-ID") UUID actorId) {
        return ResponseEntity.ok(carrierService.getAllCarriers(actorId));
    }

}
