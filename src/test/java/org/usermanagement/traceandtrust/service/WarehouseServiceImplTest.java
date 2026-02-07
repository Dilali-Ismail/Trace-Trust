package org.usermanagement.traceandtrust.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.usermanagement.traceandtrust.dto.CreateWarehouseRequest;
import org.usermanagement.traceandtrust.dto.UpdateWarehouseRequest;
import org.usermanagement.traceandtrust.dto.WarehouseDto;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.entity.Warehouse;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.DuplicateResourceException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.WarehouseMapper;
import org.usermanagement.traceandtrust.repository.UserRepository;
import org.usermanagement.traceandtrust.repository.WarehouseRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;




@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Warehouse")
public class WarehouseServiceImplTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WarehouseMapper warehouseMapper;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    private UUID adminId;
    private UUID userId;
    private UUID warehouseId;
    private User adminUser;
    private User regularUser;
    private Warehouse warehouse;
    private WarehouseDto warehouseDto;
    private CreateWarehouseRequest createRequest;
    private UpdateWarehouseRequest updateRequest;

    @BeforeEach
    void setUp() {
        // IDs
        adminId = UUID.randomUUID();
        userId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();

        // Utilisateur ADMIN
        adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@tracetrust.com");
        adminUser.setRole(Role.ADMIN);

        // Utilisateur régulier
        regularUser = new User();
        regularUser.setId(userId);
        regularUser.setName("Regular User");
        regularUser.setEmail("user@example.com");
        regularUser.setRole(Role.USER);

        // Entrepôt
        warehouse = Warehouse.builder()
                .id(warehouseId)
                .code("WH-001")
                .name("Main Warehouse")
                .active(true)
                .build();

        // WarehouseDto
        warehouseDto = WarehouseDto.builder()
                .id(warehouseId)
                .code("WH-001")
                .name("Main Warehouse")
                .active(true)
                .build();

        // Requête de création
        createRequest = new CreateWarehouseRequest();
        createRequest.setCode("WH-001");
        createRequest.setName("Main Warehouse");

        // Requête de mise à jour
        updateRequest = UpdateWarehouseRequest.builder()
                .name("Updated Warehouse Name")
                .active(true)
                .build();
    }

    // ============================================
    // TESTS - CREATE WAREHOUSE
    // ============================================

    @Test
    @DisplayName("Créer un entrepôt - Succès")
    void createWarehouse_Success() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(warehouseRepository.findByCode("WH-001")).thenReturn(Optional.empty());
        when(warehouseMapper.toEntity(any(CreateWarehouseRequest.class))).thenReturn(warehouse);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);
        when(warehouseMapper.toDto(any(Warehouse.class))).thenReturn(warehouseDto);

        // ========== ACT (When) ==========
        WarehouseDto result = warehouseService.createWarehouse(createRequest);

        // ========== ASSERT (Then) ==========
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(warehouseId);
        assertThat(result.getCode()).isEqualTo("WH-001");
        assertThat(result.getName()).isEqualTo("Main Warehouse");
        assertThat(result.isActive()).isTrue();

        verify(userRepository, times(1)).findById(adminId);
        verify(warehouseRepository, times(1)).findByCode("WH-001");
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
        verify(warehouseMapper, times(1)).toEntity(createRequest);
        verify(warehouseMapper, times(1)).toDto(warehouse);
    }

    @Test
    @DisplayName("Créer un entrepôt - Échec : Code déjà existant")
    void createWarehouse_ThrowsDuplicateResourceException_WhenCodeExists() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(warehouseRepository.findByCode("WH-001")).thenReturn(Optional.of(warehouse));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> warehouseService.createWarehouse(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Warehouse with code 'WH-001' already exists");

        verify(userRepository, times(1)).findById(adminId);
        verify(warehouseRepository, times(1)).findByCode("WH-001");
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    @DisplayName("Créer un entrepôt - Échec : Utilisateur non ADMIN")
    void createWarehouse_ThrowsForbiddenAccessException_WhenUserIsNotAdmin() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> warehouseService.createWarehouse(createRequest))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("ADMIN");

        verify(userRepository, times(1)).findById(userId);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    // ============================================
    // TESTS - GET ALL WAREHOUSES
    // ============================================

    @Test
    @DisplayName("Récupérer tous les entrepôts - Succès (uniquement actifs)")
    void getAllWarehouses_Success_ReturnsOnlyActiveWarehouses() {
        // ========== ARRANGE (Given) ==========
        UUID warehouse2Id = UUID.randomUUID();
        Warehouse warehouse2 = Warehouse.builder()
                .id(warehouse2Id)
                .code("WH-002")
                .name("Secondary Warehouse")
                .active(true)
                .build();

        WarehouseDto warehouseDto2 = WarehouseDto.builder()
                .id(warehouse2Id)
                .code("WH-002")
                .name("Secondary Warehouse")
                .active(true)
                .build();

        List<Warehouse> warehouses = Arrays.asList(warehouse, warehouse2);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(warehouseRepository.findAllByActiveTrue()).thenReturn(warehouses);
        when(warehouseMapper.toDto(warehouse)).thenReturn(warehouseDto);
        when(warehouseMapper.toDto(warehouse2)).thenReturn(warehouseDto2);

        // ========== ACT (When) ==========
        List<WarehouseDto> results = warehouseService.getAllWarehouses();

        // ========== ASSERT (Then) ==========
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getCode()).isEqualTo("WH-001");
        assertThat(results.get(1).getCode()).isEqualTo("WH-002");
        assertThat(results.get(0).isActive()).isTrue();
        assertThat(results.get(1).isActive()).isTrue();

        verify(userRepository, times(1)).findById(adminId);
        verify(warehouseRepository, times(1)).findAllByActiveTrue();
        verify(warehouseMapper, times(2)).toDto(any(Warehouse.class));
    }

    @Test
    @DisplayName("Récupérer tous les entrepôts - Échec : Utilisateur non ADMIN")
    void getAllWarehouses_ThrowsForbiddenAccessException_WhenUserIsNotAdmin() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> warehouseService.getAllWarehouses())
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("ADMIN");

        verify(userRepository, times(1)).findById(userId);
        verify(warehouseRepository, never()).findAllByActiveTrue();
    }

    // ============================================
    // TESTS - GET WAREHOUSE BY ID
    // ============================================

    @Test
    @DisplayName("Récupérer un entrepôt par ID - Succès")
    void getWarehouseById_Success() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(warehouseRepository.findByIdAndActiveTrue(warehouseId)).thenReturn(Optional.of(warehouse));
        when(warehouseMapper.toDto(warehouse)).thenReturn(warehouseDto);

        // ========== ACT (When) ==========
        WarehouseDto result = warehouseService.getWarehouseById(warehouseId);

        // ========== ASSERT (Then) ==========
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(warehouseId);
        assertThat(result.getCode()).isEqualTo("WH-001");
        assertThat(result.getName()).isEqualTo("Main Warehouse");

        verify(userRepository, times(1)).findById(adminId);
        verify(warehouseRepository, times(1)).findByIdAndActiveTrue(warehouseId);
        verify(warehouseMapper, times(1)).toDto(warehouse);
    }

    @Test
    @DisplayName("Récupérer un entrepôt par ID - Échec : Entrepôt non trouvé ou inactif")
    void getWarehouseById_ThrowsResourceNotFoundException_WhenNotFoundOrInactive() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(warehouseRepository.findByIdAndActiveTrue(warehouseId)).thenReturn(Optional.empty());

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> warehouseService.getWarehouseById(warehouseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Active warehouse with ID")
                .hasMessageContaining(warehouseId.toString());

        verify(userRepository, times(1)).findById(adminId);
        verify(warehouseRepository, times(1)).findByIdAndActiveTrue(warehouseId);
    }

    // ============================================
    // TESTS - UPDATE WAREHOUSE
    // ============================================

    @Test
    @DisplayName("Mettre à jour un entrepôt - Succès")
    void updateWarehouse_Success() {
        // ========== ARRANGE (Given) ==========
        Warehouse updatedWarehouse = Warehouse.builder()
                .id(warehouseId)
                .code("WH-001")
                .name("Updated Warehouse Name")
                .active(true)
                .build();

        WarehouseDto updatedDto = WarehouseDto.builder()
                .id(warehouseId)
                .code("WH-001")
                .name("Updated Warehouse Name")
                .active(true)
                .build();

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        doNothing().when(warehouseMapper).updateFromDto(any(UpdateWarehouseRequest.class), any(Warehouse.class));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(updatedWarehouse);
        when(warehouseMapper.toDto(any(Warehouse.class))).thenReturn(updatedDto);

        // ========== ACT (When) ==========
        WarehouseDto result = warehouseService.updateWarehouse(warehouseId, updateRequest);

        // ========== ASSERT (Then) ==========
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(warehouseId);
        assertThat(result.getName()).isEqualTo("Updated Warehouse Name");

        verify(userRepository, times(1)).findById(adminId);
        verify(warehouseRepository, times(1)).findById(warehouseId);
        verify(warehouseMapper, times(1)).updateFromDto(updateRequest, warehouse);
        verify(warehouseRepository, times(1)).save(warehouse);
        verify(warehouseMapper, times(1)).toDto(updatedWarehouse);
    }

    @Test
    @DisplayName("Mettre à jour un entrepôt - Échec : Entrepôt non trouvé")
    void updateWarehouse_ThrowsResourceNotFoundException_WhenNotFound() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> warehouseService.updateWarehouse(warehouseId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Warehouse with ID")
                .hasMessageContaining(warehouseId.toString());

        verify(userRepository, times(1)).findById(adminId);
        verify(warehouseRepository, times(1)).findById(warehouseId);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    // ============================================
    // TESTS - DELETE WAREHOUSE (Soft Delete)
    // ============================================

    @Test
    @DisplayName("Supprimer un entrepôt (soft delete) - Succès")
    void deleteWarehouse_Success_SetsActiveToFalse() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // ========== ACT (When) ==========
        warehouseService.deleteWarehouse(warehouseId);

        // ========== ASSERT (Then) ==========
        verify(userRepository, times(1)).findById(adminId);
        verify(warehouseRepository, times(1)).findById(warehouseId);
        verify(warehouseRepository, times(1)).save(warehouse);

        // Vérifier que le warehouse a été désactivé
        assertThat(warehouse.isActive()).isFalse();
    }

    @Test
    @DisplayName("Supprimer un entrepôt - Échec : Entrepôt non trouvé")
    void deleteWarehouse_ThrowsResourceNotFoundException_WhenNotFound() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> warehouseService.deleteWarehouse(warehouseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Warehouse with ID")
                .hasMessageContaining(warehouseId.toString());

        verify(userRepository, times(1)).findById(adminId);
        verify(warehouseRepository, times(1)).findById(warehouseId);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    @DisplayName("Supprimer un entrepôt - Échec : Utilisateur non ADMIN")
    void deleteWarehouse_ThrowsForbiddenAccessException_WhenUserIsNotAdmin() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> warehouseService.deleteWarehouse(warehouseId))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("ADMIN");

        verify(userRepository, times(1)).findById(userId);
        verify(warehouseRepository, never()).findById(any());
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }
}
