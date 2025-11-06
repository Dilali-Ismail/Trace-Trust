package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateSupplierRequest;
import org.usermanagement.traceandtrust.dto.SupplierDto;

import java.util.List;
import java.util.UUID;

public interface SupplierService {

    SupplierDto createSupplier(CreateSupplierRequest request, UUID actorId);
    List<SupplierDto> getAllSuppliers(UUID actorId);
}
