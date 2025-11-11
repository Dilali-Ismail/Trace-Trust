package org.usermanagement.traceandtrust.dto;

import lombok.Data;

import java.util.UUID;
@Data
public class CarrierDto {
    private UUID id;
    private String name;
    private boolean active;
}
