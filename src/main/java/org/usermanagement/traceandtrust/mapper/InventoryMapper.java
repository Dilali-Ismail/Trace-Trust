package org.usermanagement.traceandtrust.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.usermanagement.traceandtrust.dto.InventoryDto;
import org.usermanagement.traceandtrust.entity.Inventory;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    @Mapping(source = "id", target = "inventoryId")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "warehouse.code", target = "warehouseCode")
    @Mapping(target = "available", ignore = true)
    InventoryDto toDto(Inventory inventory);

    @AfterMapping
    default void calculateAvailable(@MappingTarget InventoryDto dto, Inventory inventory) {
        dto.setAvailable(inventory.getQuantity_hand() - inventory.getQuantity_reserved());
    }

}
