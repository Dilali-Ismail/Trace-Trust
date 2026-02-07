package org.usermanagement.traceandtrust.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class ReceivePurchaseOrderLineDto {

    @NotNull
    private UUID purchaseOrderlinId ;
    @Positive
    private int quantityReceived;

}
