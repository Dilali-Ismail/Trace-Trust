package org.usermanagement.traceandtrust.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SalesOrderLineDto {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @Positive(message = "Quantity must be positive")
    private int quantity;


    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;


    private String productSku;
    private String productName;
}
