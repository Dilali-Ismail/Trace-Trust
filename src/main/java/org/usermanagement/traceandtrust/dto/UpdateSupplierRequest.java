package org.usermanagement.traceandtrust.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSupplierRequest {
    @Size(min = 2, max = 100, message = "Supplier name must be between 2 and 100 characters")
    private String name;

    private String contactInfo;

    private Boolean active;
}
