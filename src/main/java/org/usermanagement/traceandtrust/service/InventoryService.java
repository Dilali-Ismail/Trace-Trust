package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateMovementRequest;
import org.usermanagement.traceandtrust.dto.InventoryDto;
import org.usermanagement.traceandtrust.entity.SalesOrderLine;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    InventoryDto recordMovement(CreateMovementRequest request, UUID actorId);
    void reserveStock(List<SalesOrderLine> orderLines, UUID warehouseId, UUID actorId);
    void releaseStock(List<SalesOrderLine> orderLines, UUID warehouseId, UUID actorId);
}

