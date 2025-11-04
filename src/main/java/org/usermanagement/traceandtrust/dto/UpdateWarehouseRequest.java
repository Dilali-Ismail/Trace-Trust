package org.usermanagement.traceandtrust.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWarehouseRequest {
    @NotBlank(message = "Warehouse name is required")
    private String name;

    @NotNull(message = "Active status is required")
    private Boolean active;
}
