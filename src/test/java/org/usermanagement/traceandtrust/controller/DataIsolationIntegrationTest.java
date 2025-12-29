package org.usermanagement.traceandtrust.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.usermanagement.traceandtrust.SecurityBaseTest;
import org.usermanagement.traceandtrust.entity.SalesOrder;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.enums.SalesOrderStatus;
import org.usermanagement.traceandtrust.repository.SalesOrderRepository;
import org.usermanagement.traceandtrust.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DataIsolationIntegrationTest extends SecurityBaseTest {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Un client devrait pouvoir annuler SA PROPRE commande")
    @WithMockUser(username = "client1@test.com", roles = "CLIENT")
    void cancelOrder_OwnOrder_ShouldSucceed() throws Exception {
        // 1. Récupérer l'utilisateur (créé par DataInitializer dans la base H2)
        User client = userRepository.findByEmail("client@test.com").orElseGet(() -> {
             User u = User.builder()
                     .email("client1@test.com")
                     .name("Client 1")
                     .password("pass")
                     .role(Role.CLIENT)
                     .enabled(true)
                     .build();
             return userRepository.save(u);
        });
        
        SalesOrder order = createTestOrder(client);

        // 2. Tenter d'annuler (authentication.name sera "client1@test.com" via @WithMockUser)
        mockMvc.perform(patch("/api/sales-orders/" + order.getId() + "/cancel"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Un client ne devrait PAS pouvoir annuler la commande d'UN AUTRE client (403)")
    @WithMockUser(username = "hacker@test.com", roles = "CLIENT")
    void cancelOrder_OtherClientOrder_ShouldReturnForbidden() throws Exception {
        // 1. Créer une commande appartenant à "client@test.com"
        User victim = userRepository.findByEmail("client@test.com").get();
        SalesOrder order = createTestOrder(victim);

        // 2. Le "hacker@test.com" tente d'annuler la commande de la victime
        mockMvc.perform(patch("/api/sales-orders/" + order.getId() + "/cancel"))
                .andExpect(status().isForbidden());
    }

    private SalesOrder createTestOrder(User client) {
        SalesOrder order = new SalesOrder();
        order.setClient(client);
        order.setStatus(SalesOrderStatus.CREATED);
        return salesOrderRepository.save(order);
    }
}
