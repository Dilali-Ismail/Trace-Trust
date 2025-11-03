package org.usermanagement.traceandtrust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.usermanagement.traceandtrust.enums.Role;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String email;
    private String name;
    private Role role;
    private boolean active;
}
