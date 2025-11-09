package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreatePurchaseOrderRequest;
import org.usermanagement.traceandtrust.dto.PurchaseOrderDto;
import org.usermanagement.traceandtrust.dto.ReceivePurchaseOrderRequest;

import java.util.UUID;

public interface PurchaseOrderService {
    PurchaseOrderDto createPurshOrder(CreatePurchaseOrderRequest orderRequest , UUID actoreID);
    PurchaseOrderDto receivePurchaseOrderItems(UUID purchaseOrderId, ReceivePurchaseOrderRequest request, UUID actorId);
}
