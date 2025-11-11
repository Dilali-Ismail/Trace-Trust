package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateSalesOrderRequest;
import org.usermanagement.traceandtrust.dto.SalesOrderDto;

import java.util.UUID;

public interface SalesOrderService {
    SalesOrderDto createSalesOrder(CreateSalesOrderRequest request, UUID actorId);
    SalesOrderDto reserveOrder(UUID orderId, UUID actorId);
    SalesOrderDto cancelOrder(UUID orderId, UUID actorId);
}
