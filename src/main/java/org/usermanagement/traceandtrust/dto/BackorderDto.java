package org.usermanagement.traceandtrust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackorderDto {
    private UUID id;
    private UUID salesOrderLineId;
    private String productSku;
    private String productName;
    private Integer quantityPending;
    private String reason;
    private Instant createdAt;
}
