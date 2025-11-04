package org.usermanagement.traceandtrust.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateWarehouseRequest {
    @NotBlank(message = "Warehouse code is required")
    @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
    private String code;

    @NotBlank(message = "Warehouse name is required")
    private String name;
}
