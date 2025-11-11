package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateShipmentRequest;
import org.usermanagement.traceandtrust.dto.ShipmentDto;

import java.util.List;
import java.util.UUID;

public interface ShipmentService {

    ShipmentDto createShipment(CreateShipmentRequest request, UUID actorId);
    ShipmentDto dispatchShipment(UUID shipmentId, UUID actorId);
    List<ShipmentDto> getAllShipments(UUID actorId);
}
