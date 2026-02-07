package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateShipmentRequest;
import org.usermanagement.traceandtrust.dto.ShipmentDto;

import java.util.List;
import java.util.UUID;

public interface ShipmentService {

    ShipmentDto createShipment(CreateShipmentRequest request);
    ShipmentDto dispatchShipment(UUID shipmentId, UUID warehouseId);
    List<ShipmentDto> getAllShipments();
    ShipmentDto getShipmentById(UUID shipmentId);
    ShipmentDto markShipmentAsDelivered(UUID shipmentId);
}
