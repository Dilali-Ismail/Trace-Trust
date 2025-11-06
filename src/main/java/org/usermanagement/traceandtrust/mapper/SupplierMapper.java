package org.usermanagement.traceandtrust.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.usermanagement.traceandtrust.dto.CreateSupplierRequest;
import org.usermanagement.traceandtrust.dto.SupplierDto;
import org.usermanagement.traceandtrust.entity.Supplier;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    SupplierDto toDto(Supplier supplier);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Supplier toEntity(CreateSupplierRequest request);
}
