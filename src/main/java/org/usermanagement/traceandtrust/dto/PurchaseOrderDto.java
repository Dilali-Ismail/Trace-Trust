package org.usermanagement.traceandtrust.dto;

import lombok.Data;
import org.usermanagement.traceandtrust.enums.PurchaseOrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class PurchaseOrderDto {

    private UUID id;
    private UUID supplierId;
    private String supplierName;
    private PurchaseOrderStatus status;
    private List<PurchaseOrderLineDto> orderLines;
    private Instant createdAt;


}
