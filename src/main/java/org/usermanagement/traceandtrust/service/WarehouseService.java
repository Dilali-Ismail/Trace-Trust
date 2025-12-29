package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateWarehouseRequest;
import org.usermanagement.traceandtrust.dto.UpdateWarehouseRequest;
import org.usermanagement.traceandtrust.dto.WarehouseDto;

import java.util.List;
import java.util.UUID;

public interface WarehouseService {
    WarehouseDto createWarehouse(CreateWarehouseRequest request);
    List<WarehouseDto> getAllWarehouses();
    WarehouseDto getWarehouseById(UUID warehouseId);
    WarehouseDto updateWarehouse(UUID warehouseId, UpdateWarehouseRequest request);
    void deleteWarehouse(UUID warehouseId);

}
