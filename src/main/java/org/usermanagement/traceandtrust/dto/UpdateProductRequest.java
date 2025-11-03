package org.usermanagement.traceandtrust.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class UpdateProductRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String category;

    @NotNull(message = "Cost price is required")
    @DecimalMin(value = "0.01", message = "Cost price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid cost price format")
    private BigDecimal costPrice;
    @NotNull(message = "Active status is required")
    private Boolean active;
}
