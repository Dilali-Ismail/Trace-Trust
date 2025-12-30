package org.usermanagement.traceandtrust.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestSecurityController {

    // 1. Test Public (Pas besoin de token)
    @GetMapping("/public")
    public String getPublic() {
        return "Je suis public.";
    }

    // 2. Test Authentifié (Besoin d'un token valide, peu importe le rôle)
    @GetMapping("/user")
    public String getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "Token Valide. User ID Keycloak : " + auth.getName();
    }

    // 3. Test Rôle ADMIN (Le plus important : vérifie votre KeycloakRoleConverter)
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAdmin() {
        return "Succès ! Le rôle ADMIN est bien détecté.";
    }
}