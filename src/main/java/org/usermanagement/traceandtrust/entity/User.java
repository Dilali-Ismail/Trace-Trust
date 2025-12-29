package org.usermanagement.traceandtrust.entity;


import jakarta.persistence.*;
import lombok.*;
import org.usermanagement.traceandtrust.enums.Role;

import java.beans.ConstructorProperties;
import java.util.EnumSet;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "account_locked")
    private boolean accountLocked = false;

    @Column(name = "failed_attempts")
    private int failedAttempts = 0;

}
