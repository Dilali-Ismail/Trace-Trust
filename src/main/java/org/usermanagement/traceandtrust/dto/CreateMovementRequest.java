package org.usermanagement.traceandtrust.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.usermanagement.traceandtrust.enums.MovementType;

import java.util.UUID;

@Data
public class CreateMovementRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Warehouse ID is required")
    private UUID warehouseId;

    @NotNull(message = "Movement type is required")
    private MovementType type;

    @Positive(message = "Quantity must be positive")
    private long quantity;

    private String referenceDocument;
}
