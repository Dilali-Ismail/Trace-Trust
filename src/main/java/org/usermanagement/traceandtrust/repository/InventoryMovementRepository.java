package org.usermanagement.traceandtrust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.usermanagement.traceandtrust.entity.InventoryMovement;

import java.util.UUID;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {

}
