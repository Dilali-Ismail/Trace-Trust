package org.usermanagement.traceandtrust.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.repository.SalesOrderRepository;
import org.usermanagement.traceandtrust.repository.ShipmentRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OwnershipService {

    private final SalesOrderRepository salesOrderRepository;
    private final ShipmentRepository shipmentRepository;

    public boolean isOrderOwner(UUID orderId, String email) {
        return salesOrderRepository.findById(orderId)
                .map(order -> order.getClient().getEmail().equals(email))
                .orElse(false);
    }

}
