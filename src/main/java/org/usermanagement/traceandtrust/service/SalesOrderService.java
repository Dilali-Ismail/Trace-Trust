package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateSalesOrderRequest;
import org.usermanagement.traceandtrust.dto.SalesOrderDto;

import java.util.UUID;

public interface SalesOrderService {
    SalesOrderDto createSalesOrder(CreateSalesOrderRequest request, UUID actorId);
}
