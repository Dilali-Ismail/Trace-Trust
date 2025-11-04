package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateWarehouseRequest;
import org.usermanagement.traceandtrust.dto.UpdateWarehouseRequest;
import org.usermanagement.traceandtrust.dto.WarehouseDto;

import java.util.List;
import java.util.UUID;

public interface WarehouseService {
    WarehouseDto createWarehouse(CreateWarehouseRequest request, UUID actorId);
    List<WarehouseDto> getAllWarehouses(UUID actorId);
    WarehouseDto getWarehouseById(UUID warehouseId, UUID actorId);
    WarehouseDto updateWarehouse(UUID warehouseId, UpdateWarehouseRequest request, UUID actorId);
    void deleteWarehouse(UUID warehouseId, UUID actorId);

}
