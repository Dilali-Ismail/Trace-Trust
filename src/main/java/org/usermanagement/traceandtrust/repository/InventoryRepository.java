package org.usermanagement.traceandtrust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.usermanagement.traceandtrust.entity.Inventory;
import org.usermanagement.traceandtrust.entity.Product;
import org.usermanagement.traceandtrust.entity.Warehouse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByProductAndWarehouse(Product product, Warehouse warehouse);
    List<Inventory> findByProduct(Product product);
}


