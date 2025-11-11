package org.usermanagement.traceandtrust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.usermanagement.traceandtrust.entity.Shipment;

import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment,UUID> {

    boolean existsBySalesOrderId(UUID salesOrderId);
}
