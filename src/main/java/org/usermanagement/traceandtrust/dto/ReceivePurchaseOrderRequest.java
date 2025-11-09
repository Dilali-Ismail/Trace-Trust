package org.usermanagement.traceandtrust.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReceivePurchaseOrderRequest {
    @NotNull
    private UUID warehouseId;

    @NotEmpty(message = "Reception must contain at least one line")
    @Valid
    private List<ReceivePurchaseOrderLineDto> receivedLines;

    private String referenceDocument;

}
