package org.usermanagement.traceandtrust.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.usermanagement.traceandtrust.SecurityBaseTest;
import org.usermanagement.traceandtrust.dto.LoginRequest;
import org.usermanagement.traceandtrust.dto.RefreshTokenRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TokenLifecycleIntegrationTest extends SecurityBaseTest {

    @Test
    @DisplayName("Devrait rafraîchir le token et appliquer la rotation")
    void refreshToken_Success_WithRotation() throws Exception {
        // 1. Login initial pour obtenir les tokens
        LoginRequest loginRequest = new LoginRequest("admin@test.com", "admin123");
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String oldRefreshToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.refreshToken");

        // 2. Demander un rafraîchissement
        RefreshTokenRequest refreshReq = new RefreshTokenRequest(oldRefreshToken);
        MvcResult refreshResult = mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String newRefreshToken = JsonPath.read(refreshResult.getResponse().getContentAsString(), "$.refreshToken");

        // 3. Vérifier que l'ancien token ne fonctionne plus (Rotation)
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Le logout devrait révoquer le refresh token")
    void logout_ShouldRevokeToken() throws Exception {
        // 1. Login
        LoginRequest loginRequest = new LoginRequest("admin@test.com", "admin123");
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String tokenToRevoke = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.refreshToken");

        // 2. Logout
        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequest(tokenToRevoke))))
                .andExpect(status().isOk());

        // 3. Vérifier que le token est inutilisable
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequest(tokenToRevoke))))
                .andExpect(status().isUnauthorized());
    }
}
