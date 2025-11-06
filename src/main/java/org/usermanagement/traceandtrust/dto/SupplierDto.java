package org.usermanagement.traceandtrust.dto;


import lombok.Data;

import java.util.UUID;

@Data
public class SupplierDto {
    private UUID id;
    private String name;
    private String contactInfo;
    private boolean active;
}
