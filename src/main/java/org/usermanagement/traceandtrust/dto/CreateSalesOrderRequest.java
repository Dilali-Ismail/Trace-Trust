package org.usermanagement.traceandtrust.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateSalesOrderRequest {

    @NotEmpty(message = "Sales order must have at least one line")
    @Valid
    private List<SalesOrderLineDto> orderLines;
}
