package org.usermanagement.traceandtrust.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.usermanagement.traceandtrust.SecurityBaseTest;
import org.usermanagement.traceandtrust.dto.LoginRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerIntegrationTest extends SecurityBaseTest {

    @Test
    @DisplayName("Devrait se connecter avec succès et retourner des tokens")
    void login_Success() throws Exception {
        // Préparation de la requête (On utilise l'admin créé par DataInitializer)
        LoginRequest loginRequest = new LoginRequest("admin@test.com", "admin123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    @DisplayName("Devrait échouer avec un mot de passe incorrect (401)")
    void login_WrongPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@test.com", "mauvais_pass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentification échouée"));
    }

    @Test
    @DisplayName("Devrait échouer avec un email inexistant (401)")
    void login_UserNotFound() throws Exception {
        LoginRequest loginRequest = new LoginRequest("inconnu@test.com", "admin123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}