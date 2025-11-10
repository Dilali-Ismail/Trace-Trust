package org.usermanagement.traceandtrust.dto;

import lombok.Data;
import org.usermanagement.traceandtrust.enums.SalesOrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class SalesOrderDto {
    private UUID id;
    private UUID clientId;
    private UUID warehouseId;
    private SalesOrderStatus status;
    private List<SalesOrderLineDto> orderLines;
    private Instant createdAt;
}
