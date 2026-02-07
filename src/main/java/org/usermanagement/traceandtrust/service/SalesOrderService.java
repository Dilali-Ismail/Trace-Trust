package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateSalesOrderRequest;
import org.usermanagement.traceandtrust.dto.SalesOrderDto;

import java.util.List;
import java.util.UUID;

public interface SalesOrderService {
    SalesOrderDto createSalesOrder(CreateSalesOrderRequest request);
    SalesOrderDto reserveOrder(UUID orderId, UUID warehouseId);
    SalesOrderDto requestReservation(UUID orderId, UUID warehouseId);
    SalesOrderDto cancelOrder(UUID orderId, UUID warehouseId);
    List<SalesOrderDto> getAllSalesOrders();
    SalesOrderDto getSalesOrderById(UUID id);
}
