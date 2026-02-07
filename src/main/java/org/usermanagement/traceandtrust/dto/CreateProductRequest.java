package org.usermanagement.traceandtrust.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
    private String sku;

    @NotBlank(message = "Name is required")
    private String name;

    private String category;

    @NotNull(message = "Cost price is required")
    @DecimalMin(value = "0.01", message = "Cost price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid cost price format. Max 8 digits for integer part and 2 for fraction part.")
    private BigDecimal costPrice;
}
