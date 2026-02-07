package org.usermanagement.traceandtrust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.usermanagement.traceandtrust.entity.Carrier;

import java.util.UUID;

public interface CarrierRepository extends JpaRepository<Carrier, UUID> {
    Object existsByName(String s);
}
