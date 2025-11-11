package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CarrierDto;
import org.usermanagement.traceandtrust.dto.CreateCarrierRequest;

import java.util.List;
import java.util.UUID;

public interface CarrierService {
    CarrierDto createCarrier(CreateCarrierRequest request, UUID actorId);
    List<CarrierDto> getAllCarriers(UUID actorId);
}
