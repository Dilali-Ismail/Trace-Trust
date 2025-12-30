package org.usermanagement.traceandtrust.entity;


import jakarta.persistence.*;
import lombok.*;
import org.usermanagement.traceandtrust.enums.Role;

import java.util.EnumSet;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String Name;

//    @Column(nullable = false)
//    private String password;
    @Column(name = "keycloak_id", unique = true)
     private String keycloakId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

}
