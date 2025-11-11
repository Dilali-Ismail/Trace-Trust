package org.usermanagement.traceandtrust.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCarrierRequest {
    @NotBlank(message = "Carrier name is required")
    private String name;
}
