package org.usermanagement.traceandtrust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResult {
    private boolean fullyReserved;
    private Map<UUID, Integer> reservedQuantities; // productId -> reservedQuantity
    private Map<UUID, Integer> backorderQuantities; // productId -> backorderQuantity
}
