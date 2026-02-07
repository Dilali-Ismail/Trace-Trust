package org.usermanagement.traceandtrust.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sales_order_backorders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderBackorder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "sales_order_line_id", nullable = false)
    private SalesOrderLine salesOrderLine;

    @Column(nullable = false)
    private Integer quantityPending;

    private String reason;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
