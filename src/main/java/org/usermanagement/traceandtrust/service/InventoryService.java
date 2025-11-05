package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateMovementRequest;
import org.usermanagement.traceandtrust.dto.InventoryDto;

import java.util.UUID;

public interface InventoryService {
    InventoryDto recordMovement(CreateMovementRequest request, UUID actorId);
}

