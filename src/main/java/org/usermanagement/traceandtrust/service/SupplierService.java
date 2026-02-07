package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateSupplierRequest;
import org.usermanagement.traceandtrust.dto.SupplierDto;
import org.usermanagement.traceandtrust.dto.UpdateSupplierRequest;

import java.util.List;
import java.util.UUID;

public interface SupplierService {

    SupplierDto createSupplier(CreateSupplierRequest request);
    List<SupplierDto> getAllSuppliers();
    SupplierDto getSupplierById(UUID id);
    SupplierDto updateSupplier(UUID id, UpdateSupplierRequest request);
    void deleteSupplier(UUID id);
}
