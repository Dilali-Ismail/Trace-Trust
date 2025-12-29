package org.usermanagement.traceandtrust.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.usermanagement.traceandtrust.dto.CreateSalesOrderRequest;
import org.usermanagement.traceandtrust.dto.SalesOrderDto;
import org.usermanagement.traceandtrust.dto.SalesOrderLineDto;
import org.usermanagement.traceandtrust.entity.*;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.enums.SalesOrderStatus;
import org.usermanagement.traceandtrust.exception.BusinessException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.SalesOrderMapper;
import org.usermanagement.traceandtrust.repository.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour SalesOrderServiceImpl
 *
 * Fonctionnalités testées :
 * - createSalesOrder : Création par un CLIENT
 * - reserveOrder : Réservation de stock
 * - cancelOrder : Annulation avec gestion du stock
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service SalesOrder")
class SalesOrderServiceImplTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private SalesOrderMapper salesOrderMapper;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private SalesOrderServiceImpl salesOrderService;

    // Données de test
    private UUID clientId;
    private UUID adminId;
    private UUID userId;
    private UUID orderId;
    private UUID warehouseId;
    private UUID productId;
    private User client;
    private User admin;
    private User regularUser;
    private Warehouse warehouse;
    private Product product;
    private SalesOrder salesOrder;
    private SalesOrderLine orderLine;
    private SalesOrderDto salesOrderDto;
    private CreateSalesOrderRequest createRequest;

    @BeforeEach
    void setUp() {
        // IDs
        clientId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        productId = UUID.randomUUID();

        // Users
        client = new User();
        client.setId(clientId);
        client.setName("Client User");
        client.setRole(Role.CLIENT);

        admin = new User();
        admin.setId(adminId);
        admin.setName("Admin User");
        admin.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(userId);
        regularUser.setName("Regular User");
        regularUser.setRole(Role.USER);

        // Warehouse
        warehouse = new Warehouse();
        warehouse.setId(warehouseId);
        warehouse.setCode("WH-001");
        warehouse.setName("Main Warehouse");

        // Product
        product = new Product();
        product.setId(productId);
        product.setSku("PROD-001");
        product.setName("Test Product");
        product.setActive(true);

        // SalesOrderLine
        orderLine = new SalesOrderLine();
        orderLine.setProduct(product);
        orderLine.setQuantity(10);
        orderLine.setUnitPrice(BigDecimal.valueOf(50.00));

        // SalesOrder
        salesOrder = new SalesOrder();
        salesOrder.setId(orderId);
        salesOrder.setClient(client);
        salesOrder.setWarehouse(warehouse);
        salesOrder.setStatus(SalesOrderStatus.CREATED);
        salesOrder.getOrderLines().add(orderLine);

        // SalesOrderDto
        salesOrderDto = new SalesOrderDto();
        salesOrderDto.setId(orderId);
        salesOrderDto.setStatus(SalesOrderStatus.CREATED);

        // CreateSalesOrderRequest
        SalesOrderLineDto lineDto = new SalesOrderLineDto();
        lineDto.setProductId(productId);
        lineDto.setQuantity(10);
        lineDto.setUnitPrice(BigDecimal.valueOf(50.00));

        createRequest = new CreateSalesOrderRequest();
        createRequest.setWarehouseId(warehouseId);
        createRequest.setOrderLines(Arrays.asList(lineDto));
    }

    // ============================================
    // TESTS - CREATE SALES ORDER
    // ============================================

    @Test
    @DisplayName("Créer une commande de vente - Succès avec CLIENT")
    void createSalesOrder_Success_WithClient() {
        // ARRANGE
        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(salesOrderMapper.toDto(any(SalesOrder.class))).thenReturn(salesOrderDto);

        // ACT
        SalesOrderDto result = salesOrderService.createSalesOrder(createRequest);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        assertThat(result.getStatus()).isEqualTo(SalesOrderStatus.CREATED);

        verify(userRepository, times(1)).findById(clientId);
        verify(warehouseRepository, times(1)).findById(warehouseId);
        verify(productRepository, times(1)).findById(productId);
        verify(salesOrderRepository, times(1)).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Créer une commande de vente - Échec : Utilisateur non CLIENT")
    void createSalesOrder_ThrowsForbiddenAccessException_WhenNotClient() {
        // ARRANGE
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // ACT & ASSERT
        assertThatThrownBy(() -> salesOrderService.createSalesOrder(createRequest))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("Only CLIENT users can create sales orders");

        verify(warehouseRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Créer une commande de vente - Échec : Produit inactif")
    void createSalesOrder_ThrowsBusinessException_WhenProductInactive() {
        // ARRANGE
        product.setActive(false);

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // ACT & ASSERT
        assertThatThrownBy(() -> salesOrderService.createSalesOrder(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available for sale");

        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Créer une commande de vente - Échec : Warehouse non trouvé")
    void createSalesOrder_ThrowsResourceNotFoundException_WhenWarehouseNotFound() {
        // ARRANGE
        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> salesOrderService.createSalesOrder(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Warehouse not found");

        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Créer une commande de vente - Échec : Produit non trouvé")
    void createSalesOrder_ThrowsResourceNotFoundException_WhenProductNotFound() {
        // ARRANGE
        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> salesOrderService.createSalesOrder(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Créer une commande de vente - Échec : Utilisateur non trouvé")
    void createSalesOrder_ThrowsResourceNotFoundException_WhenUserNotFound() {
        // ARRANGE
        when(userRepository.findById(clientId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> salesOrderService.createSalesOrder(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Actor not found");

        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    // ============================================
    // TESTS - RESERVE ORDER
    // ============================================

    @Test
    @DisplayName("Réserver une commande - Succès")
    void reserveOrder_Success() {
        // ARRANGE
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        doNothing().when(inventoryService).reserveStock(any(), any());
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(salesOrderMapper.toDto(any(SalesOrder.class))).thenReturn(salesOrderDto);

        // ACT
        SalesOrderDto result = salesOrderService.reserveOrder(orderId);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(salesOrder.getStatus()).isEqualTo(SalesOrderStatus.RESERVED);

        verify(salesOrderRepository, times(1)).findById(orderId);
        verify(inventoryService, times(1)).reserveStock(salesOrder.getOrderLines(), warehouseId);
        verify(salesOrderRepository, times(1)).save(salesOrder);
    }

    @Test
    @DisplayName("Réserver une commande - Échec : Statut incorrect")
    void reserveOrder_ThrowsBusinessException_WhenStatusNotCreated() {
        // ARRANGE
        salesOrder.setStatus(SalesOrderStatus.RESERVED);

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        // ACT & ASSERT
        assertThatThrownBy(() -> salesOrderService.reserveOrder(orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only orders with CREATED status can be reserved");

        verify(inventoryService, never()).reserveStock(any(), any());
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Réserver une commande - Échec : Commande non trouvée")
    void reserveOrder_ThrowsResourceNotFoundException_WhenOrderNotFound() {
        // ARRANGE
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> salesOrderService.reserveOrder(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sales Order not found");

        verify(inventoryService, never()).reserveStock(any(), any());
    }

    // ============================================
    // TESTS - CANCEL ORDER
    // ============================================

    @Test
    @DisplayName("Annuler une commande CREATED - Succès")
    void cancelOrder_Success_WhenStatusCreated() {
        // ARRANGE
        salesOrder.setStatus(SalesOrderStatus.CREATED);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(salesOrderMapper.toDto(any(SalesOrder.class))).thenReturn(salesOrderDto);

        // ACT
        SalesOrderDto result = salesOrderService.cancelOrder(orderId);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(salesOrder.getStatus()).isEqualTo(SalesOrderStatus.CANCELED);

        verify(userRepository, times(1)).findById(adminId);
        verify(salesOrderRepository, times(1)).findById(orderId);
        verify(inventoryService, never()).releaseStock(any(), any());
        verify(salesOrderRepository, times(1)).save(salesOrder);
    }

    @Test
    @DisplayName("Annuler une commande RESERVED - Succès avec libération de stock")
    void cancelOrder_Success_WhenStatusReserved() {
        // ARRANGE
        salesOrder.setStatus(SalesOrderStatus.RESERVED);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        doNothing().when(inventoryService).releaseStock(any(), any());
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(salesOrderMapper.toDto(any(SalesOrder.class))).thenReturn(salesOrderDto);

        // ACT
        SalesOrderDto result = salesOrderService.cancelOrder(orderId);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(salesOrder.getStatus()).isEqualTo(SalesOrderStatus.CANCELED);

        verify(inventoryService, times(1)).releaseStock(salesOrder.getOrderLines(), warehouseId);
        verify(salesOrderRepository, times(1)).save(salesOrder);
    }

    @Test
    @DisplayName("Annuler une commande - Échec : Déjà SHIPPED")
    void cancelOrder_ThrowsBusinessException_WhenStatusShipped() {
        // ARRANGE
        salesOrder.setStatus(SalesOrderStatus.SHIPPED);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        // ACT & ASSERT
        assertThatThrownBy(() -> salesOrderService.cancelOrder(orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot cancel an order that has already been shipped or delivered");

        verify(inventoryService, never()).releaseStock(any(), any());
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Annuler une commande - Échec : Déjà DELIVERED")
    void cancelOrder_ThrowsBusinessException_WhenStatusDelivered() {
        // ARRANGE
        salesOrder.setStatus(SalesOrderStatus.DELIVERED);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        // ACT & ASSERT
        assertThatThrownBy(() -> salesOrderService.cancelOrder(orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot cancel an order that has already been shipped or delivered");

        verify(inventoryService, never()).releaseStock(any(), any());
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Annuler une commande - Échec : Déjà CANCELED")
    void cancelOrder_ThrowsBusinessException_WhenAlreadyCanceled() {
        // ARRANGE
        salesOrder.setStatus(SalesOrderStatus.CANCELED);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        // ACT & ASSERT
        assertThatThrownBy(() -> salesOrderService.cancelOrder(orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("This order has already been canceled");

        verify(inventoryService, never()).releaseStock(any(), any());
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Annuler une commande - Échec : Utilisateur non ADMIN")
    void cancelOrder_ThrowsForbiddenAccessException_WhenNotAdmin() {
        // ARRANGE
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // ACT & ASSERT
        assertThatThrownBy(() -> salesOrderService.cancelOrder(orderId))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("This operation is restricted to ADMIN users");

        verify(salesOrderRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Annuler une commande - Échec : Commande non trouvée")
    void cancelOrder_ThrowsResourceNotFoundException_WhenOrderNotFound() {
        // ARRANGE
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> salesOrderService.cancelOrder(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sales Order not found");

        verify(inventoryService, never()).releaseStock(any(), any());
    }

    @Test
    @DisplayName("Annuler une commande - Échec : Acteur non trouvé")
    void cancelOrder_ThrowsResourceNotFoundException_WhenActorNotFound() {
        // ARRANGE
        when(userRepository.findById(adminId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> salesOrderService.cancelOrder(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Actor not found");

        verify(salesOrderRepository, never()).findById(any());
    }
}
