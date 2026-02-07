package org.usermanagement.traceandtrust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.usermanagement.traceandtrust.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySku(String sku);
    Optional<Product> findBySkuAndActiveTrue(String sku);
    Optional<Product> findByIdAndActiveTrue(UUID id);
    List<Product> findAllByActiveTrue();
    List<Product> findByCategoryAndActiveTrue(String category);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p.category FROM Product p WHERE p.active = true AND p.category IS NOT NULL")
    List<String> findAllDistinctCategories();
}
