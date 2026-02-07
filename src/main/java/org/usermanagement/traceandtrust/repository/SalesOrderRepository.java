package org.usermanagement.traceandtrust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.usermanagement.traceandtrust.entity.SalesOrder;

import java.util.Collection;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {

    java.util.List<SalesOrder> findAllByClientEmail(String email);
}
