package org.usermanagement.traceandtrust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.usermanagement.traceandtrust.entity.Product;
import org.usermanagement.traceandtrust.entity.SalesOrderLine;
import org.usermanagement.traceandtrust.enums.SalesOrderStatus;

import java.util.List;
import java.util.UUID;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, UUID> {
    long countByProductAndSalesOrder_StatusIn(Product product, List<SalesOrderStatus> statuses);
}
