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
    @Mapping(target = "backorders", expression = "java(mapBackorders(salesOrder))")
    SalesOrderDto toDto(SalesOrder salesOrder);

    default java.util.List<org.usermanagement.traceandtrust.dto.BackorderDto> mapBackorders(SalesOrder salesOrder) {
        if (salesOrder.getOrderLines() == null) return java.util.Collections.emptyList();
        return salesOrder.getOrderLines().stream()
                .filter(line -> line.getBackorder() != null)
                .map(line -> toBackorderDto(line.getBackorder()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Mapping(source = "salesOrderLine.id", target = "salesOrderLineId")
    @Mapping(source = "salesOrderLine.product.sku", target = "productSku")
    @Mapping(source = "salesOrderLine.product.name", target = "productName")
    org.usermanagement.traceandtrust.dto.BackorderDto toBackorderDto(org.usermanagement.traceandtrust.entity.SalesOrderBackorder backorder);


    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "product.name", target = "productName")
    SalesOrderLineDto toDto(SalesOrderLine salesOrderLine);
}
