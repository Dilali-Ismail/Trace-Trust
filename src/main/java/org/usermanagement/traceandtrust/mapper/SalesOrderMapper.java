package org.usermanagement.traceandtrust.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.usermanagement.traceandtrust.dto.SalesOrderDto;
import org.usermanagement.traceandtrust.dto.SalesOrderLineDto;
import org.usermanagement.traceandtrust.entity.SalesOrder;
import org.usermanagement.traceandtrust.entity.SalesOrderLine;

@Mapper(componentModel = "spring")
public interface SalesOrderMapper {
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    SalesOrderDto toDto(SalesOrder salesOrder);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "product.name", target = "productName")
    SalesOrderLineDto toDto(SalesOrderLine salesOrderLine);
}
