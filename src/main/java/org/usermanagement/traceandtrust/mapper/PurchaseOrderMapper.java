package org.usermanagement.traceandtrust.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.usermanagement.traceandtrust.dto.PurchaseOrderDto;
import org.usermanagement.traceandtrust.dto.PurchaseOrderLineDto;
import org.usermanagement.traceandtrust.entity.PurchaseOrder;
import org.usermanagement.traceandtrust.entity.PurchaseOrderLine;

@Mapper(componentModel = "spring")
public interface PurchaseOrderMapper {
    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    PurchaseOrderDto toDto(PurchaseOrder purchaseOrder);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "product.name", target = "productName")
    PurchaseOrderLineDto toDto(PurchaseOrderLine purchaseOrderLine);



}
