package org.usermanagement.traceandtrust.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.usermanagement.traceandtrust.dto.CreateShipmentRequest;
import org.usermanagement.traceandtrust.dto.ShipmentDto;
import org.usermanagement.traceandtrust.entity.*;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.enums.SalesOrderStatus;
import org.usermanagement.traceandtrust.enums.ShipmentStatus;
import org.usermanagement.traceandtrust.exception.BusinessException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.ShipmentMapper;
import org.usermanagement.traceandtrust.repository.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ShipmentServiceImpl
 *
 * Fonctionnalités testées :
 * - createShipment : Création d'une expédition
 * - getAllShipments : Liste des expéditions
 * - dispatchShipment : Expédition du colis
 * - markShipmentAsDelivered : Marquer comme livré
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Shipment")
class ShipmentServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private CarrierRepository carrierRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShipmentMapper shipmentMapper;

    @Mock
    private StockService inventoryService;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    // Données de test
    private UUID warehouseManagerId;
    private UUID adminId;
    private UUID userId;
    private UUID shipmentId;
    private UUID salesOrderId;
    private UUID carrierId;
    private UUID warehouseId;
    private User warehouseManager;
    private User admin;
    private User regularUser;
    private SalesOrder salesOrder;
    private Carrier carrier;
    private Warehouse warehouse;
    private Shipment shipment;
    private ShipmentDto shipmentDto;
    private CreateShipmentRequest createRequest;

    @BeforeEach
    void setUp() {
        // IDs
        warehouseManagerId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        userId = UUID.randomUUID();
        shipmentId = UUID.randomUUID();
        salesOrderId = UUID.randomUUID();
        carrierId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();

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

        // Warehouse
        warehouse = new Warehouse();
        warehouse.setId(warehouseId);
        warehouse.setCode("WH-001");
        warehouse.setName("Main Warehouse");

        // Carrier
        carrier = new Carrier();
        carrier.setId(carrierId);
        carrier.setName("DHL Express");
        carrier.setActive(true);

        salesOrder = new SalesOrder();
        salesOrder.setId(salesOrderId);
        salesOrder.setStatus(SalesOrderStatus.RESERVED);
        salesOrder.setOrderLines(Arrays.asList());

        // Shipment
        shipment = new Shipment();
        shipment.setId(shipmentId);
        shipment.setSalesOrder(salesOrder);
        shipment.setCarrier(carrier);
        shipment.setTrackingNumber("TRK-12345");
        shipment.setStatus(ShipmentStatus.PLANNED);

        // ShipmentDto
        shipmentDto = new ShipmentDto();
        shipmentDto.setId(shipmentId);
        shipmentDto.setTrackingNumber("TRK-12345");
        shipmentDto.setStatus(ShipmentStatus.PLANNED);

        // Create Request
        createRequest = new CreateShipmentRequest();
        createRequest.setSalesOrderId(salesOrderId);
        createRequest.setCarrierId(carrierId);
        createRequest.setTrackingNumber("TRK-12345");
    }

    // ============================================
    // TESTS - CREATE SHIPMENT
    // ============================================

    @Test
    @DisplayName("Créer une expédition - Succès avec WAREHOUSE_MANAGER")
    void createShipment_Success_WithWarehouseManager() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));
        when(carrierRepository.findById(carrierId)).thenReturn(Optional.of(carrier));
        when(shipmentRepository.existsBySalesOrderId(salesOrderId)).thenReturn(false);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
        when(shipmentMapper.toDto(any(Shipment.class))).thenReturn(shipmentDto);

        // ACT
        ShipmentDto result = shipmentService.createShipment(createRequest);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getTrackingNumber()).isEqualTo("TRK-12345");
        assertThat(result.getStatus()).isEqualTo(ShipmentStatus.PLANNED);

        verify(userRepository, times(1)).findById(warehouseManagerId);
        verify(salesOrderRepository, times(1)).findById(salesOrderId);
        verify(carrierRepository, times(1)).findById(carrierId);
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    @DisplayName("Créer une expédition - Succès avec ADMIN")
    void createShipment_Success_WithAdmin() {
        // ARRANGE
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));
        when(carrierRepository.findById(carrierId)).thenReturn(Optional.of(carrier));
        when(shipmentRepository.existsBySalesOrderId(salesOrderId)).thenReturn(false);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
        when(shipmentMapper.toDto(any(Shipment.class))).thenReturn(shipmentDto);

        // ACT
        ShipmentDto result = shipmentService.createShipment(createRequest);

        // ASSERT
        assertThat(result).isNotNull();
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    @DisplayName("Créer une expédition - Échec : SalesOrder pas en statut RESERVED")
    void createShipment_ThrowsBusinessException_WhenOrderNotReserved() {
        // ARRANGE
        salesOrder.setStatus(SalesOrderStatus.CREATED);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));
        when(carrierRepository.findById(carrierId)).thenReturn(Optional.of(carrier));

        // ACT & ASSERT
        assertThatThrownBy(() -> shipmentService.createShipment(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("RESERVED status");

        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    @DisplayName("Créer une expédition - Échec : Carrier inactif")
    void createShipment_ThrowsBusinessException_WhenCarrierInactive() {
        // ARRANGE
        carrier.setActive(false);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));
        when(carrierRepository.findById(carrierId)).thenReturn(Optional.of(carrier));

        // ACT & ASSERT
        assertThatThrownBy(() -> shipmentService.createShipment(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inactive carrier");

        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    @DisplayName("Créer une expédition - Échec : Expédition déjà existante pour cette commande")
    void createShipment_ThrowsBusinessException_WhenShipmentAlreadyExists() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));
        when(carrierRepository.findById(carrierId)).thenReturn(Optional.of(carrier));
        when(shipmentRepository.existsBySalesOrderId(salesOrderId)).thenReturn(true);

        // ACT & ASSERT
        assertThatThrownBy(() -> shipmentService.createShipment(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("shipment already exists");

        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    @DisplayName("Créer une expédition - Échec : Utilisateur non autorisé")
    void createShipment_ThrowsForbiddenAccessException_WhenUserNotAuthorized() {
        // ARRANGE
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // ACT & ASSERT
        assertThatThrownBy(() -> shipmentService.createShipment(createRequest))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("WAREHOUSE_MANAGER or ADMIN");

        verify(salesOrderRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Créer une expédition - Échec : SalesOrder non trouvée")
    void createShipment_ThrowsResourceNotFoundException_WhenSalesOrderNotFound() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> shipmentService.createShipment(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sales Order not found");

        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    @DisplayName("Créer une expédition - Échec : Carrier non trouvé")
    void createShipment_ThrowsResourceNotFoundException_WhenCarrierNotFound() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));
        when(carrierRepository.findById(carrierId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> shipmentService.createShipment(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Carrier not found");

        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    // ============================================
    // TESTS - GET ALL SHIPMENTS
    // ============================================

    @Test
    @DisplayName("Récupérer toutes les expéditions - Succès")
    void getAllShipments_Success() {
        // ARRANGE
        Shipment shipment2 = new Shipment();
        shipment2.setId(UUID.randomUUID());
        shipment2.setTrackingNumber("TRK-67890");

        List<Shipment> shipments = Arrays.asList(shipment, shipment2);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(shipmentRepository.findAll()).thenReturn(shipments);
        when(shipmentMapper.toDto(any(Shipment.class))).thenReturn(shipmentDto);

        // ACT
        List<ShipmentDto> results = shipmentService.getAllShipments();

        // ASSERT
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);

        verify(shipmentRepository, times(1)).findAll();
        verify(shipmentMapper, times(2)).toDto(any(Shipment.class));
    }

    // ============================================
    // TESTS - DISPATCH SHIPMENT
    // ============================================

    @Test
    @DisplayName("Expédier une commande - Succès")
    void dispatchShipment_Success() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        doNothing().when(inventoryService).dispatchStock(any(), any());
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
        when(shipmentMapper.toDto(any(Shipment.class))).thenReturn(shipmentDto);

        // ACT
        ShipmentDto result = shipmentService.dispatchShipment(shipmentId, warehouseId);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(shipment.getShippedAt()).isNotNull();
        assertThat(salesOrder.getStatus()).isEqualTo(SalesOrderStatus.SHIPPED);

        verify(inventoryService, times(1)).dispatchStock(any(), eq(warehouseId));
        verify(shipmentRepository, times(1)).save(shipment);
        verify(salesOrderRepository, times(1)).save(salesOrder);
    }

    @Test
    @DisplayName("Expédier une commande - Échec : Statut pas PLANNED")
    void dispatchShipment_ThrowsBusinessException_WhenNotPlanned() {
        // ARRANGE
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));

        // ACT & ASSERT
        assertThatThrownBy(() -> shipmentService.dispatchShipment(shipmentId, warehouseId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("status is PLANNED");

        verify(inventoryService, never()).dispatchStock(any(), any());
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    @DisplayName("Expédier une commande - Échec : Shipment non trouvé")
    void dispatchShipment_ThrowsResourceNotFoundException_WhenShipmentNotFound() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> shipmentService.dispatchShipment(shipmentId, warehouseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Shipment not found");

        verify(inventoryService, never()).dispatchStock(any(), any());
    }

    // ============================================
    // TESTS - MARK AS DELIVERED
    // ============================================

    @Test
    @DisplayName("Marquer comme livré - Succès")
    void markShipmentAsDelivered_Success() {
        // ARRANGE
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
        when(shipmentMapper.toDto(any(Shipment.class))).thenReturn(shipmentDto);

        // ACT
        ShipmentDto result = shipmentService.markShipmentAsDelivered(shipmentId);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(shipment.getDeliveredAt()).isNotNull();
        assertThat(salesOrder.getStatus()).isEqualTo(SalesOrderStatus.DELIVERED);

        verify(shipmentRepository, times(1)).save(shipment);
        verify(salesOrderRepository, times(1)).save(salesOrder);
    }

    @Test
    @DisplayName("Marquer comme livré - Échec : Statut pas IN_TRANSIT")
    void markShipmentAsDelivered_ThrowsBusinessException_WhenNotInTransit() {
        // ARRANGE
        shipment.setStatus(ShipmentStatus.PLANNED);

        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));

        // ACT & ASSERT
        assertThatThrownBy(() -> shipmentService.markShipmentAsDelivered(shipmentId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("IN_TRANSIT");

        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    @DisplayName("Marquer comme livré - Échec : Shipment non trouvé")
    void markShipmentAsDelivered_ThrowsResourceNotFoundException_WhenNotFound() {
        // ARRANGE
        when(userRepository.findById(warehouseManagerId)).thenReturn(Optional.of(warehouseManager));
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> shipmentService.markShipmentAsDelivered(shipmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Shipment not found");

        verify(shipmentRepository, never()).save(any(Shipment.class));
    }
}
