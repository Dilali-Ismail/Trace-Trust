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
import org.usermanagement.traceandtrust.dto.ReservationResult;
import org.usermanagement.traceandtrust.entity.*;
import org.usermanagement.traceandtrust.enums.MovementType;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.BusinessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
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
 * Tests unitaires pour StockServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Stock")
class StockServiceImplTest {

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
    private StockServiceImpl stockService;

    // Données de test
    private UUID productId;
    private UUID warehouseId;
    private Product product;
    private Warehouse warehouse;
    private Inventory inventory;
    private InventoryDto inventoryDto;
    private CreateMovementRequest movementRequest;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();

        product = new Product();
        product.setId(productId);
        product.setSku("PROD-001");
        product.setName("Test Product");
        product.setActive(true);

        warehouse = new Warehouse();
        warehouse.setId(warehouseId);
        warehouse.setCode("WH-001");
        warehouse.setName("Main Warehouse");
        warehouse.setActive(true);

        inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .product(product)
                .warehouse(warehouse)
                .quantity_hand(100L)
                .quantity_reserved(20L)
                .build();

        inventoryDto = new InventoryDto();
        inventoryDto.setProductId(productId);
        inventoryDto.setWarehouseId(warehouseId);
        inventoryDto.setQuantity_hand(100L);
        inventoryDto.setQuantity_reserved(20L);

        movementRequest = new CreateMovementRequest();
        movementRequest.setProductId(productId);
        movementRequest.setWarehouseId(warehouseId);
        movementRequest.setType(MovementType.INBOUND);
        movementRequest.setQuantity(50);
        movementRequest.setReferenceDocument("PO-123");
    }

    @Test
    @DisplayName("Enregistrer un mouvement INBOUND - Succès")
    void recordMovement_Inbound_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(inventoryDto);

        InventoryDto result = stockService.recordMovement(movementRequest);

        assertThat(result).isNotNull();
        assertThat(inventory.getQuantity_hand()).isEqualTo(150L);

        verify(inventoryRepository, times(1)).save(inventory);
        verify(inventoryMovement, times(1)).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Enregistrer un mouvement ADJUSTMENT - Succès")
    void recordMovement_Adjustment_Success() {
        movementRequest.setType(MovementType.ADJUSTMENT);
        movementRequest.setQuantity(200);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(inventoryDto);

        InventoryDto result = stockService.recordMovement(movementRequest);

        assertThat(result).isNotNull();
        assertThat(inventory.getQuantity_hand()).isEqualTo(200L);

        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    @DisplayName("Réserver du stock - Succès total")
    void reserveStock_TotalSuccess() {
        SalesOrderLine orderLine = new SalesOrderLine();
        orderLine.setProduct(product);
        orderLine.setQuantity(30);

        List<SalesOrderLine> orderLines = Arrays.asList(orderLine);

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));

        ReservationResult result = stockService.reserveStock(orderLines, warehouseId);

        assertThat(result.isFullyReserved()).isTrue();
        assertThat(result.getReservedQuantities().get(productId)).isEqualTo(30);
        assertThat(inventory.getQuantity_reserved()).isEqualTo(50L);

        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    @DisplayName("Réserver du stock - Succès partiel (Backorder)")
    void reserveStock_PartialSuccess() {
        SalesOrderLine orderLine = new SalesOrderLine();
        orderLine.setProduct(product);
        orderLine.setQuantity(100); // Disponible : 100 - 20 = 80

        List<SalesOrderLine> orderLines = Arrays.asList(orderLine);

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));

        ReservationResult result = stockService.reserveStock(orderLines, warehouseId);

        assertThat(result.isFullyReserved()).isFalse();
        assertThat(result.getReservedQuantities().get(productId)).isEqualTo(80);
        assertThat(result.getBackorderQuantities().get(productId)).isEqualTo(20);
        assertThat(inventory.getQuantity_reserved()).isEqualTo(100L);

        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    @DisplayName("Libérer du stock - Succès")
    void releaseStock_Success() {
        SalesOrderLine orderLine = new SalesOrderLine();
        orderLine.setProduct(product);
        orderLine.setQuantity(10);

        List<SalesOrderLine> orderLines = Arrays.asList(orderLine);

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));

        stockService.releaseStock(orderLines, warehouseId);

        assertThat(inventory.getQuantity_reserved()).isEqualTo(10L);
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    @DisplayName("Expédier du stock - Succès")
    void dispatchStock_Success() {
        SalesOrderLine orderLine = new SalesOrderLine();
        orderLine.setProduct(product);
        orderLine.setQuantity(15);

        List<SalesOrderLine> orderLines = Arrays.asList(orderLine);

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));

        stockService.dispatchStock(orderLines, warehouseId);

        assertThat(inventory.getQuantity_hand()).isEqualTo(85L);
        assertThat(inventory.getQuantity_reserved()).isEqualTo(5L);

        verify(inventoryRepository, times(1)).save(inventory);
        verify(inventoryMovement, times(1)).save(any(InventoryMovement.class));
    }
}
