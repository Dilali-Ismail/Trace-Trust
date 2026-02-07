package org.usermanagement.traceandtrust.dto;

import lombok.Data;
import org.usermanagement.traceandtrust.enums.ShipmentStatus;

import java.time.Instant;
import java.util.UUID;

@Data
public class ShipmentDto {
    private UUID id;
    private UUID salesOrderId;
    private UUID carrierId;
    private String carrierName;
    private ShipmentStatus status;
    private String trackingNumber;
    private Instant createdAt;
    private Instant shippedAt;
    private Instant deliveredAt;
}
