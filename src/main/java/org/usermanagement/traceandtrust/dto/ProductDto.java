package org.usermanagement.traceandtrust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private UUID id;
    private String sku;
    private String name;
    private String category;
    private BigDecimal costPrice;
    private boolean active;
    private String imageUrl;
}
