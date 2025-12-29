package org.usermanagement.traceandtrust.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.usermanagement.traceandtrust.SecurityBaseTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductControllerSecurityTest extends SecurityBaseTest {

    @Test
    @DisplayName("ADMIN devrait pouvoir accéder au endpoint de création de produit")
    @WithMockUser(roles = "ADMIN")
    void createProduct_AsAdmin_ShouldSucceed() throws Exception {
        String validProduct = "{\"sku\": \"PROD-001\", \"name\": \"Produit Test\", \"costPrice\": 10.00}";

        mockMvc.perform(post("/api/products/create")
                        .contentType("application/json")
                        .content(validProduct))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("CLIENT ne devrait PAS pouvoir accéder au endpoint de création (403)")
    @WithMockUser(roles = "CLIENT")
    void createProduct_AsClient_ShouldReturnForbidden() throws Exception {
        String validProduct = "{\"sku\": \"PROD-002\", \"name\": \"Autre Produit\", \"costPrice\": 15.00}";

        mockMvc.perform(post("/api/products/create")
                        .contentType("application/json")
                        .content(validProduct))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Accès sans token devrait retourner 401 après config")
    void getAllProducts_Anonymous_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("N'importe quel utilisateur authentifié peut voir les produits")
    @WithMockUser(roles = "CLIENT")
    void getAllProducts_AsAuthenticated_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }
}
