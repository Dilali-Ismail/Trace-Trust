package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateSalesOrderRequest;
import org.usermanagement.traceandtrust.dto.SalesOrderDto;

import java.util.List;
import java.util.UUID;

public interface SalesOrderService {
    SalesOrderDto createSalesOrder(CreateSalesOrderRequest request);
    SalesOrderDto reserveOrder(UUID orderId);
    SalesOrderDto cancelOrder(UUID orderId);
    List<SalesOrderDto> getAllSalesOrders();
}
