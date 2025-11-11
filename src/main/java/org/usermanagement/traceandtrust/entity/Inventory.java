package org.usermanagement.traceandtrust.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "warehouse_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @ManyToOne(optional = false , fetch = FetchType.LAZY)
    @JoinColumn(name = "prodcut_id",nullable = false)
    private Product product;

    @ManyToOne(optional = false , fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id",nullable = false)
    private Warehouse warehouse;

    @Column(name = "qty_on_hand", nullable = false)
    private long quantity_hand = 0 ;

    @Column(name = "qty_reserved", nullable = false)
    private long  quantity_reserved = 0;

    @CreationTimestamp
    private Instant Created_at ;

    @UpdateTimestamp
    private Instant updatedAt;
}
