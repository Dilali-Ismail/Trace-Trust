package org.usermanagement.traceandtrust.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateShipmentRequest {

    @NotNull(message = "Sales Order ID is required")
    private UUID salesOrderId;

    @NotNull(message = "Carrier ID is required")
    private UUID carrierId;

    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;
}