package org.usermanagement.traceandtrust.dto;

import lombok.Data;

import java.util.UUID;
@Data
public class InventoryDto {

    private UUID inventoryId;
    private UUID productId;
    private String productSku;
    private UUID warehouseId;
    private String warehouseCode;
    private long quantity_hand;
    private long quantity_reserved;
    private long available;
}
