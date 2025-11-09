package org.usermanagement.traceandtrust.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreatePurchaseOrderRequest {

    @NotNull
    private UUID supplierId;

    @NotEmpty
    @Valid
    private List<PurchaseOrderLineDto> orderLines;

}
