package org.usermanagement.traceandtrust.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.usermanagement.traceandtrust.dto.CarrierDto;
import org.usermanagement.traceandtrust.dto.CreateCarrierRequest;
import org.usermanagement.traceandtrust.entity.Carrier;

@Mapper(componentModel = "spring")
public interface CarrierMapper {
    CarrierDto todto(Carrier carrier);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Carrier toEntity(CreateCarrierRequest request);

    CarrierDto toDto(Carrier save);
}
