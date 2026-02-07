package org.usermanagement.traceandtrust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.usermanagement.traceandtrust.entity.SalesOrderBackorder;

import java.util.UUID;

@Repository
public interface SalesOrderBackorderRepository extends JpaRepository<SalesOrderBackorder, UUID> {
}
