package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateSalesOrderRequest;
import org.usermanagement.traceandtrust.dto.SalesOrderDto;
import org.usermanagement.traceandtrust.entity.Product;
import org.usermanagement.traceandtrust.enums.SalesOrderStatus;

import java.util.List;
import java.util.UUID;

public interface SalesOrderService {
    SalesOrderDto createSalesOrder(CreateSalesOrderRequest request, UUID actorId);
}
