package org.usermanagement.traceandtrust.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.usermanagement.traceandtrust.dto.ShipmentDto;
import org.usermanagement.traceandtrust.entity.Shipment;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {
    @Mapping(source = "salesOrder.id", target = "salesOrderId")
    @Mapping(source = "carrier.id", target = "carrierId")
    @Mapping(source = "carrier.name", target = "carrierName")
    ShipmentDto toDto(Shipment shipment);
}
