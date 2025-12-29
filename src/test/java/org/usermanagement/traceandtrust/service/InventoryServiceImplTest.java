package org.usermanagement.traceandtrust.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.usermanagement.traceandtrust.dto.CreateMovementRequest;
import org.usermanagement.traceandtrust.dto.InventoryDto;
import org.usermanagement.traceandtrust.entity.*;
import org.usermanagement.traceandtrust.enums.MovementType;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.BusinessException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.exception.StockUnavailableException;
import org.usermanagement.traceandtrust.mapper.InventoryMapper;
import org.usermanagement.traceandtrust.repository.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour InventoryServiceImpl
 *
 * Service complexe testant :
 * - recordMovement : INBOUND et ADJUSTMENT
 * - reserveStock : Réservation de stock
 * - releaseStock : Libération de réservation
 * - dispatchStock : Expédition de stock
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Inventory")
class InventoryServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMovementRepository inventoryMovement;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    // Données de test
    private UUID warehouseManagerId;
    private UUID adminId;
    private UUID productId;
    private UUID warehouseId;
    private User warehouseManager;
    private User admin;
    private Product product;
    private Warehouse warehouse;
    private Inventory inventory;
    private InventoryDto inventoryDto;
    private CreateMovementRequest movementRequest;

    @BeforeEach
    void setUp() {
        // IDs
        warehouseManagerId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        productId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();

        // WAREHOUSE_MANAGER
        warehouseManager = new User();
        warehouseManager.setId(warehouseManagerId);
        warehouseManager.setName("Warehouse Manager");
        warehouseManager.setEmail("manager@tracetrust.com");
        warehouseManager.setRole(Role.WAREHOUSE_MANAGER);

        // ADMIN (n'a pas le droit de gérer le stock)
        admin = new User();
        admin.setId(adminId);
        admin.setName("Admin");
        admin.setEmail("admin@tracetrust.com");
        admin.setRole(Role.ADMIN);

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
        warehouse.setActive(true);

        // Inventory
        inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .product(product)
                .warehouse(warehouse)
                .quantity_hand(100L)
                .quantity_reserved(20L)
                .build();

        // InventoryDto
        inventoryDto = new InventoryDto();
        inventoryDto.setProductId(productId);
        inventoryDto.setWarehouseId(warehouseId);
        inventoryDto.setQuantity_hand(100L);
        inventoryDto.setQuantity_reserved(20L);

        // Movement Request
        movementRequest = new CreateMovementRequest();
        movementRequest.setProductId(productId);
        movementRequest.setWarehouseId(warehouseId);
        movementRequest.setType(MovementType.INBOUND);
        movementRequest.setQuantity(50);
        movementRequest.setReferenceDocument("PO-123");
    }

    // ============================================
    // TESTS - RECORD MOVEMENT (INBOUND)
    // ============================================

    @Test
    @DisplayName("Enregistrer un mouvement INBOUND - Succès")
    void recordMovement_Inbound_Success() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMovement.save(any(InventoryMovement.class))).thenReturn(null);
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(inventoryDto);

        // ACT
        InventoryDto result = inventoryService.recordMovement(movementRequest);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(inventory.getQuantity_hand()).isEqualTo(150L); // 100 + 50

        verify(userRepository, times(1)).findById(warehouseManagerId);
        verify(productRepository, times(1)).findById(productId);
        verify(warehouseRepository, times(1)).findById(warehouseId);
        verify(inventoryRepository, times(1)).save(inventory);
        verify(inventoryMovement, times(1)).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Enregistrer un mouvement INBOUND - Création automatique d'inventaire si inexistant")
    void recordMovement_Inbound_CreatesNewInventory() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.empty());

        Inventory newInventory = Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity_hand(50L)
                .build();

        when(inventoryRepository.save(any(Inventory.class))).thenReturn(newInventory);
        when(inventoryMovement.save(any(InventoryMovement.class))).thenReturn(null);
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(inventoryDto);

        // ACT
        InventoryDto result = inventoryService.recordMovement(movementRequest);

        // ASSERT
        assertThat(result).isNotNull();
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    // ============================================
    // TESTS - RECORD MOVEMENT (ADJUSTMENT)
    // ============================================

    @Test
    @DisplayName("Enregistrer un mouvement ADJUSTMENT - Succès")
    void recordMovement_Adjustment_Success() {
        // ARRANGE
        movementRequest.setType(MovementType.ADJUSTMENT);
        movementRequest.setQuantity(200); // Nouvelle quantité absolue

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMovement.save(any(InventoryMovement.class))).thenReturn(null);
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(inventoryDto);

        // ACT
        InventoryDto result = inventoryService.recordMovement(movementRequest);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(inventory.getQuantity_hand()).isEqualTo(200L); // Valeur absolue

        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    @DisplayName("Enregistrer un mouvement ADJUSTMENT - Échec : Quantité négative")
    void recordMovement_Adjustment_ThrowsException_WhenNegativeQuantity() {
        // ARRANGE
        movementRequest.setType(MovementType.ADJUSTMENT);
        movementRequest.setQuantity(-10);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));

        // ACT & ASSERT
        assertThatThrownBy(() -> inventoryService.recordMovement(movementRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Adjustment quantity cannot be a negative value");

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Enregistrer un mouvement ADJUSTMENT - Échec : Quantité inférieure aux réservations")
    void recordMovement_Adjustment_ThrowsException_WhenBelowReserved() {
        // ARRANGE
        inventory.setQuantity_reserved(50L);
        movementRequest.setType(MovementType.ADJUSTMENT);
        movementRequest.setQuantity(30); // Moins que réservé (50)

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));

        // ACT & ASSERT
        assertThatThrownBy(() -> inventoryService.recordMovement(movementRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("new quantity on hand cannot be less than reserved quantity");

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Enregistrer un mouvement - Échec : Utilisateur non WAREHOUSE_MANAGER")
    void recordMovement_ThrowsForbiddenAccessException_WhenNotWarehouseManager() {
        // ARRANGE
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // ACT & ASSERT
        assertThatThrownBy(() -> inventoryService.recordMovement(movementRequest))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("WAREHOUSE_MANAGER");

        verify(productRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Enregistrer un mouvement - Échec : Produit non trouvé")
    void recordMovement_ThrowsResourceNotFoundException_WhenProductNotFound() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> inventoryService.recordMovement(movementRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    // ============================================
    // TESTS - RESERVE STOCK
    // ============================================

    @Test
    @DisplayName("Réserver du stock - Succès")
    void reserveStock_Success() {
        // ARRANGE
        SalesOrderLine orderLine = new SalesOrderLine();
        orderLine.setProduct(product);
        orderLine.setQuantity(30);

        List<SalesOrderLine> orderLines = Arrays.asList(orderLine);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // ACT
        inventoryService.reserveStock(orderLines, warehouseId);

        // ASSERT
        assertThat(inventory.getQuantity_reserved()).isEqualTo(50L); // 20 + 30

        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    @DisplayName("Réserver du stock - Échec : Stock insuffisant")
    void reserveStock_ThrowsStockUnavailableException_WhenInsufficientStock() {
        // ARRANGE
        SalesOrderLine orderLine = new SalesOrderLine();
        orderLine.setProduct(product);
        orderLine.setQuantity(100); // Plus que disponible (100 - 20 = 80)

        List<SalesOrderLine> orderLines = Arrays.asList(orderLine);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));

        // ACT & ASSERT
        assertThatThrownBy(() -> inventoryService.reserveStock(orderLines, warehouseId))
                .isInstanceOf(StockUnavailableException.class)
                .hasMessageContaining("Insufficient stock");

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Réserver du stock - Échec : Inventaire inexistant")
    void reserveStock_ThrowsStockUnavailableException_WhenNoInventory() {
        // ARRANGE
        SalesOrderLine orderLine = new SalesOrderLine();
        orderLine.setProduct(product);
        orderLine.setQuantity(10);

        List<SalesOrderLine> orderLines = Arrays.asList(orderLine);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> inventoryService.reserveStock(orderLines, warehouseId))
                .isInstanceOf(StockUnavailableException.class)
                .hasMessageContaining("No stock available");

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    // ============================================
    // TESTS - RELEASE STOCK
    // ============================================

    @Test
    @DisplayName("Libérer du stock - Succès")
    void releaseStock_Success() {
        // ARRANGE
        SalesOrderLine orderLine = new SalesOrderLine();
        orderLine.setProduct(product);
        orderLine.setQuantity(10);

        List<SalesOrderLine> orderLines = Arrays.asList(orderLine);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // ACT
        inventoryService.releaseStock(orderLines, warehouseId);

        // ASSERT
        assertThat(inventory.getQuantity_reserved()).isEqualTo(10L); // 20 - 10

        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    @DisplayName("Libérer du stock - Ne peut pas être négatif")
    void releaseStock_CannotBeNegative() {
        // ARRANGE
        inventory.setQuantity_reserved(5L);

        SalesOrderLine orderLine = new SalesOrderLine();
        orderLine.setProduct(product);
        orderLine.setQuantity(10); // Plus que réservé

        List<SalesOrderLine> orderLines = Arrays.asList(orderLine);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // ACT
        inventoryService.releaseStock(orderLines, warehouseId);

        // ASSERT
        assertThat(inventory.getQuantity_reserved()).isEqualTo(0L); // Math.max(0, 5-10)

        verify(inventoryRepository, times(1)).save(inventory);
    }

    // ============================================
    // TESTS - DISPATCH STOCK
    // ============================================

    @Test
    @DisplayName("Expédier du stock - Succès")
    void dispatchStock_Success() {
        // ARRANGE
        SalesOrderLine orderLine = new SalesOrderLine();
        orderLine.setProduct(product);
        orderLine.setQuantity(15);

        List<SalesOrderLine> orderLines = Arrays.asList(orderLine);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMovement.save(any(InventoryMovement.class))).thenReturn(null);

        // ACT
        inventoryService.dispatchStock(orderLines, warehouseId);

        // ASSERT
        assertThat(inventory.getQuantity_hand()).isEqualTo(85L); // 100 - 15
        assertThat(inventory.getQuantity_reserved()).isEqualTo(5L); // 20 - 15

        verify(inventoryRepository, times(1)).save(inventory);
        verify(inventoryMovement, times(1)).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Expédier du stock - Échec : Stock insuffisant")
    void dispatchStock_ThrowsBusinessException_WhenInsufficientStock() {
        // ARRANGE
        SalesOrderLine orderLine = new SalesOrderLine();
        orderLine.setProduct(product);
        orderLine.setQuantity(150); // Plus que disponible

        List<SalesOrderLine> orderLines = Arrays.asList(orderLine);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));

        // ACT & ASSERT
        assertThatThrownBy(() -> inventoryService.dispatchStock(orderLines, warehouseId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Inconsistent stock levels");

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Expédier du stock - Échec : Inventaire non trouvé")
    void dispatchStock_ThrowsBusinessException_WhenInventoryNotFound() {
        // ARRANGE
        SalesOrderLine orderLine = new SalesOrderLine();
        orderLine.setProduct(product);
        orderLine.setQuantity(10);

        List<SalesOrderLine> orderLines = Arrays.asList(orderLine);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> inventoryService.dispatchStock(orderLines, warehouseId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Inventory record not found");

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }
}
