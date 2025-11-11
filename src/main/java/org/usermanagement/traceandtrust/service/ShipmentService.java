package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateShipmentRequest;
import org.usermanagement.traceandtrust.dto.ShipmentDto;

import java.util.UUID;

public interface ShipmentService {

    ShipmentDto createShipment(CreateShipmentRequest request, UUID actorId);
}
