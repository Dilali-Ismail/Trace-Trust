package org.usermanagement.traceandtrust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseDto {
    private UUID id;
    private String code;
    private String name;
    private boolean active;
}
