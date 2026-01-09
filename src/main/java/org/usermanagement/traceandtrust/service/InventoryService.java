package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateMovementRequest;
import org.usermanagement.traceandtrust.dto.InventoryDto;
import org.usermanagement.traceandtrust.entity.SalesOrderLine;

import org.usermanagement.traceandtrust.dto.InventoryMovementDto;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    InventoryDto recordMovement(CreateMovementRequest request);
    void reserveStock(List<SalesOrderLine> orderLines, UUID warehouseId);
    void releaseStock(List<SalesOrderLine> orderLines, UUID warehouseId);
    void dispatchStock(List<SalesOrderLine> orderLines, UUID warehouseId);
    List<InventoryDto> getStock(UUID warehouseId, UUID productId);
    List<InventoryMovementDto> getHistory(UUID warehouseId, UUID productId);
}

