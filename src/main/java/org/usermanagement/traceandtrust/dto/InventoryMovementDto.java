package org.usermanagement.traceandtrust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.usermanagement.traceandtrust.enums.MovementType;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementDto {
    private UUID id;
    private UUID productId;
    private String productSku;
    private UUID warehouseId;
    private String warehouseName;
    private MovementType type;
    private long quantity;
    private String referenceDocument;
    private Instant occurredAt;
}
