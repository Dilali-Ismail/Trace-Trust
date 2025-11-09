package org.usermanagement.traceandtrust.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PurchaseOrderLineDto {
    @NotNull
    private UUID productId;

    @Positive(message = "Ordered quantity must be positive")
    private int quantityOrdered;

    private BigDecimal unitPrice;

    private String productSku;
    private String productName;
    private int quantityReceived;

}
