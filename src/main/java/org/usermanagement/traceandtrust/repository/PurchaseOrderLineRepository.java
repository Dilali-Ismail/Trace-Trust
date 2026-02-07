package org.usermanagement.traceandtrust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.usermanagement.traceandtrust.dto.PurchaseOrderLineDto;
import org.usermanagement.traceandtrust.entity.PurchaseOrderLine;

import java.util.UUID;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, UUID> {

}
