package org.usermanagement.traceandtrust.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PurchaseOrderLineDto {
    private UUID id;  // The order line's own ID (needed for receiving)

    @NotNull
    private UUID productId;

    @Positive(message = "Ordered quantity must be positive")
    private int quantityOrdered;

    private BigDecimal unitPrice;

    private String productSku;
    private String productName;
    private int quantityReceived;

}
