package org.usermanagement.traceandtrust.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.usermanagement.traceandtrust.dto.*;
import org.usermanagement.traceandtrust.entity.*;
import org.usermanagement.traceandtrust.enums.PurchaseOrderStatus;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.BusinessException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.PurchaseOrderMapper;
import org.usermanagement.traceandtrust.repository.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PurchaseOrderServiceImpl
 *
 * Fonctionnalités testées :
 * - createPurshOrder : Création d'une commande d'achat
 * - receivePurchaseOrderItems : Réception d'articles
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service PurchaseOrder")
class PurchaseOrderServiceImplTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderMapper purchaseOrderMapper;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockService inventoryService;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    // Données de test
    private UUID warehouseManagerId;
    private UUID adminId;
    private UUID userId;
    private UUID purchaseOrderId;
    private UUID supplierId;
    private UUID productId;
    private UUID warehouseId;
    private UUID lineId;
    private User warehouseManager;
    private User admin;
    private User regularUser;
    private Supplier supplier;
    private Product product;
    private Warehouse warehouse;
    private PurchaseOrder purchaseOrder;
    private PurchaseOrderLine orderLine;
    private PurchaseOrderDto purchaseOrderDto;
    private CreatePurchaseOrderRequest createRequest;
    private ReceivePurchaseOrderRequest receiveRequest;

    @BeforeEach
    void setUp() {
        // IDs
        warehouseManagerId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        userId = UUID.randomUUID();
        purchaseOrderId = UUID.randomUUID();
        supplierId = UUID.randomUUID();
        productId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        lineId = UUID.randomUUID();

        // Users
        warehouseManager = new User();
        warehouseManager.setId(warehouseManagerId);
        warehouseManager.setName("Warehouse Manager");
        warehouseManager.setRole(Role.WAREHOUSE_MANAGER);

        admin = new User();
        admin.setId(adminId);
        admin.setName("Admin");
        admin.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(userId);
        regularUser.setName("Regular User");
        regularUser.setRole(Role.USER);

        // Supplier
        supplier = new Supplier();
        supplier.setId(supplierId);
        supplier.setName("Acme Supplier");
        supplier.setActive(true);

        // Product
        product = new Product();
        product.setId(productId);
        product.setSku("PROD-001");
        product.setName("Test Product");
        product.setActive(true);

        // Warehouse
        warehouse = new Warehouse();
        warehouse.setId(warehouseId);
        warehouse.setCode("WH-001");
        warehouse.setName("Main Warehouse");

        // PurchaseOrderLine
        orderLine = new PurchaseOrderLine();
        orderLine.setId(lineId);
        orderLine.setProduct(product);
        orderLine.setQuantityOrdered(100);
        orderLine.setQuantityReceived(0);
        orderLine.setUnitPrice(BigDecimal.valueOf(10.00));

        // PurchaseOrder
        purchaseOrder = new PurchaseOrder();
        purchaseOrder.setId(purchaseOrderId);
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);
        purchaseOrder.getOrderLines().add(orderLine);

        // PurchaseOrderDto
        purchaseOrderDto = new PurchaseOrderDto();
        purchaseOrderDto.setId(purchaseOrderId);
        purchaseOrderDto.setStatus(PurchaseOrderStatus.APPROVED);

        // CreatePurchaseOrderRequest
        PurchaseOrderLineDto lineDto = new PurchaseOrderLineDto();
        lineDto.setProductId(productId);
        lineDto.setQuantityOrdered(100);
        lineDto.setUnitPrice(BigDecimal.valueOf(10.00));

        createRequest = new CreatePurchaseOrderRequest();
        createRequest.setSupplierId(supplierId);
        createRequest.setOrderLines(Arrays.asList(lineDto));

        // ReceivePurchaseOrderRequest
        ReceivePurchaseOrderLineDto receiveLineDto = new ReceivePurchaseOrderLineDto();
        receiveLineDto.setPurchaseOrderlinId(lineId);
        receiveLineDto.setQuantityReceived(50);

        receiveRequest = new ReceivePurchaseOrderRequest();
        receiveRequest.setWarehouseId(warehouseId);
        receiveRequest.setReceivedLines(Arrays.asList(receiveLineDto));
    }

    // ============================================
    // TESTS - CREATE PURCHASE ORDER
    // ============================================

    @Test
    @DisplayName("Créer une commande d'achat - Succès avec WAREHOUSE_MANAGER")
    void createPurshOrder_Success_WithWarehouseManager() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(purchaseOrderDto);

        // ACT
        PurchaseOrderDto result = purchaseOrderService.createPurshOrder(createRequest);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(purchaseOrderId);
        assertThat(result.getStatus()).isEqualTo(PurchaseOrderStatus.APPROVED);

        verify(userRepository, times(1)).findById(warehouseManagerId);
        verify(supplierRepository, times(1)).findById(supplierId);
        verify(productRepository, times(1)).findById(productId);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Créer une commande d'achat - Succès avec ADMIN")
    void createPurshOrder_Success_WithAdmin() {
        // ARRANGE
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(purchaseOrderDto);

        // ACT
        PurchaseOrderDto result = purchaseOrderService.createPurshOrder(createRequest);

        // ASSERT
        assertThat(result).isNotNull();
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Créer une commande d'achat - Échec : Utilisateur non autorisé")
    void createPurshOrder_ThrowsForbiddenAccessException_WhenUserNotAuthorized() {
        // ARRANGE
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // ACT & ASSERT
        assertThatThrownBy(() -> purchaseOrderService.createPurshOrder(createRequest))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("WAREHOUSE_MANAGER or ADMIN");

        verify(supplierRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Créer une commande d'achat - Échec : Supplier non trouvé")
    void createPurshOrder_ThrowsResourceNotFoundException_WhenSupplierNotFound() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> purchaseOrderService.createPurshOrder(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found");

        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Créer une commande d'achat - Échec : Product non trouvé")
    void createPurshOrder_ThrowsResourceNotFoundException_WhenProductNotFound() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> purchaseOrderService.createPurshOrder(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    // ============================================
    // TESTS - RECEIVE PURCHASE ORDER ITEMS
    // ============================================

    @Test
    @DisplayName("Recevoir des articles - Réception partielle")
    void receivePurchaseOrderItems_PartialReception_Success() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(purchaseOrderRepository.findById(purchaseOrderId)).thenReturn(Optional.of(purchaseOrder));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        InventoryDto mockInventoryDto = new InventoryDto();
        mockInventoryDto.setProductId(productId);
        mockInventoryDto.setWarehouseId(warehouseId);
        mockInventoryDto.setQuantity_hand(50L);
        when(inventoryService.recordMovement(any(CreateMovementRequest.class)))
                .thenReturn(mockInventoryDto);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(purchaseOrderDto);

        // ACT
        PurchaseOrderDto result = purchaseOrderService.receivePurchaseOrderItems(purchaseOrderId, receiveRequest);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(orderLine.getQuantityReceived()).isEqualTo(50); // 0 + 50
        assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseOrderStatus.PARTIALLY_RECEIVED);

        verify(inventoryService, times(1)).recordMovement(any());
        verify(purchaseOrderRepository, times(1)).save(purchaseOrder);
    }

    @Test
    @DisplayName("Recevoir des articles - Réception complète")
    void receivePurchaseOrderItems_FullReception_Success() {
        // ARRANGE
        ReceivePurchaseOrderLineDto receiveLineDto = new ReceivePurchaseOrderLineDto();
        receiveLineDto.setPurchaseOrderlinId(lineId);
        receiveLineDto.setQuantityReceived(100); // Quantité complète

        receiveRequest.setReceivedLines(Arrays.asList(receiveLineDto));

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(purchaseOrderRepository.findById(purchaseOrderId)).thenReturn(Optional.of(purchaseOrder));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        InventoryDto mockInventoryDto = new InventoryDto();
        mockInventoryDto.setProductId(productId);
        mockInventoryDto.setWarehouseId(warehouseId);
        mockInventoryDto.setQuantity_hand(50L);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(purchaseOrderDto);

        // ACT
        PurchaseOrderDto result = purchaseOrderService.receivePurchaseOrderItems(purchaseOrderId, receiveRequest);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(orderLine.getQuantityReceived()).isEqualTo(100);
        assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);

        verify(inventoryService, times(1)).recordMovement(any());
        verify(purchaseOrderRepository, times(1)).save(purchaseOrder);
    }

    @Test
    @DisplayName("Recevoir des articles - Échec : Quantité dépasse commandée")
    void receivePurchaseOrderItems_ThrowsBusinessException_WhenExceedsOrdered() {
        // ARRANGE
        orderLine.setQuantityReceived(60); // Déjà reçu 60

        ReceivePurchaseOrderLineDto receiveLineDto = new ReceivePurchaseOrderLineDto();
        receiveLineDto.setPurchaseOrderlinId(lineId);
        receiveLineDto.setQuantityReceived(50); // Veut recevoir 50 de plus (total = 110 > 100)

        receiveRequest.setReceivedLines(Arrays.asList(receiveLineDto));

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(purchaseOrderRepository.findById(purchaseOrderId)).thenReturn(Optional.of(purchaseOrder));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));

        // ACT & ASSERT
        assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrderItems(purchaseOrderId, receiveRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot receive more items than ordered");

        verify(inventoryService, never()).recordMovement(any());
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Recevoir des articles - Échec : Commande déjà reçue")
    void receivePurchaseOrderItems_ThrowsBusinessException_WhenAlreadyReceived() {
        // ARRANGE
        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(purchaseOrderRepository.findById(purchaseOrderId)).thenReturn(Optional.of(purchaseOrder));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));

        // ACT & ASSERT
        assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrderItems(purchaseOrderId, receiveRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already closed or canceled");

        verify(inventoryService, never()).recordMovement(any());
    }

    @Test
    @DisplayName("Recevoir des articles - Échec : Commande annulée")
    void receivePurchaseOrderItems_ThrowsBusinessException_WhenCanceled() {
        // ARRANGE
        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELED);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(purchaseOrderRepository.findById(purchaseOrderId)).thenReturn(Optional.of(purchaseOrder));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));

        // ACT & ASSERT
        assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrderItems(purchaseOrderId, receiveRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already closed or canceled");

        verify(inventoryService, never()).recordMovement(any());
    }

    @Test
    @DisplayName("Recevoir des articles - Échec : PurchaseOrder non trouvée")
    void receivePurchaseOrderItems_ThrowsResourceNotFoundException_WhenPurchaseOrderNotFound() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(purchaseOrderRepository.findById(purchaseOrderId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrderItems(purchaseOrderId, receiveRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Purchase Order not found");

        verify(inventoryService, never()).recordMovement(any());
    }

    @Test
    @DisplayName("Recevoir des articles - Échec : Warehouse non trouvé")
    void receivePurchaseOrderItems_ThrowsResourceNotFoundException_WhenWarehouseNotFound() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(purchaseOrderRepository.findById(purchaseOrderId)).thenReturn(Optional.of(purchaseOrder));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrderItems(purchaseOrderId, receiveRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Warehouse not found");

        verify(inventoryService, never()).recordMovement(any());
    }

    @Test
    @DisplayName("Recevoir des articles - Échec : OrderLine non trouvée")
    void receivePurchaseOrderItems_ThrowsResourceNotFoundException_WhenOrderLineNotFound() {
        // ARRANGE
        UUID wrongLineId = UUID.randomUUID();
        ReceivePurchaseOrderLineDto receiveLineDto = new ReceivePurchaseOrderLineDto();
        receiveLineDto.setPurchaseOrderlinId(wrongLineId);
        receiveLineDto.setQuantityReceived(50);

        receiveRequest.setReceivedLines(Arrays.asList(receiveLineDto));

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(purchaseOrderRepository.findById(purchaseOrderId)).thenReturn(Optional.of(purchaseOrder));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));

        // ACT & ASSERT
        assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrderItems(purchaseOrderId, receiveRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order line with id")
                .hasMessageContaining("not found");

        verify(inventoryService, never()).recordMovement(any());
    }

    @Test
    @DisplayName("Recevoir des articles - Échec : Utilisateur non autorisé")
    void receivePurchaseOrderItems_ThrowsForbiddenAccessException_WhenUserNotAuthorized() {
        // ARRANGE
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // ACT & ASSERT
        assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrderItems(purchaseOrderId, receiveRequest))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("WAREHOUSE_MANAGER or ADMIN");

        verify(purchaseOrderRepository, never()).findById(any());
    }
}
