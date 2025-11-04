package org.usermanagement.traceandtrust.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.usermanagement.traceandtrust.dto.CreateWarehouseRequest;
import org.usermanagement.traceandtrust.dto.UpdateWarehouseRequest;
import org.usermanagement.traceandtrust.dto.WarehouseDto;
import org.usermanagement.traceandtrust.entity.Warehouse;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    WarehouseDto toDto(Warehouse warehouse);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Warehouse toEntity(CreateWarehouseRequest request);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateFromDto(UpdateWarehouseRequest request, @MappingTarget Warehouse warehouse);
}
