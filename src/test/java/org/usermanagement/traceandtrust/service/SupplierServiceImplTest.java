package org.usermanagement.traceandtrust.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.usermanagement.traceandtrust.dto.CreateSupplierRequest;
import org.usermanagement.traceandtrust.dto.SupplierDto;
import org.usermanagement.traceandtrust.entity.Supplier;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.DuplicateResourceException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.SupplierMapper;
import org.usermanagement.traceandtrust.repository.SupplierRepository;
import org.usermanagement.traceandtrust.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Supplier")
public class SupplierServiceImplTest {
    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private UUID adminId;
    private UUID nonAdminId;
    private UUID supplierId;
    private User adminUser;
    private User nonAdminUser;
    private Supplier supplier;
    private SupplierDto supplierDto;
    private CreateSupplierRequest createRequest;

    @BeforeEach
    void setUp() {
        // IDs
        adminId = UUID.randomUUID();
        nonAdminId = UUID.randomUUID();
        supplierId = UUID.randomUUID();

        // Utilisateur ADMIN
        adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setName("admin");
        adminUser.setRole(Role.ADMIN);

        // Utilisateur NON ADMIN
        nonAdminUser = new User();
        nonAdminUser.setId(nonAdminId);
        nonAdminUser.setName("user");
        nonAdminUser.setRole(Role.USER);

        // Créer un Supplier (entité)
        supplier = new Supplier();
        supplier.setId(supplierId);
        supplier.setName("Acme Corporation");
        supplier.setContactInfo("contact@acme.com");
        supplier.setActive(true);

        // Créer un SupplierDto
        supplierDto = new SupplierDto();
        supplierDto.setId(supplierId);
        supplierDto.setName("Acme Corporation");
        supplierDto.setContactInfo("contact@acme.com");
        supplierDto.setActive(true);

        // Créer une requête de création
        createRequest = new CreateSupplierRequest();
        createRequest.setName("Acme Corporation");
        createRequest.setContactInfo("contact@acme.com");
    }
    @Test
    @DisplayName("Créer un fournisseur - Succès avec utilisateur ADMIN")
    void createSupplier_Success_WithAdminUser() {
        // ========== ARRANGE (Given) ==========
        // Simuler que l'utilisateur est ADMIN
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));

        // Simuler qu'aucun fournisseur avec ce nom n'existe
        when(supplierRepository.findByName("Acme Corporation")).thenReturn(Optional.empty());

        // Simuler que le mapper convertit la requête en entité
        when(supplierMapper.toEntity(any(CreateSupplierRequest.class))).thenReturn(supplier);

        // Simuler que le repository sauvegarde le fournisseur
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);

        // Simuler que le mapper convertit l'entité en DTO
        when(supplierMapper.toDto(any(Supplier.class))).thenReturn(supplierDto);

        // ========== ACT (When) ==========
        SupplierDto result = supplierService.createSupplier(createRequest, adminId);

        // ========== ASSERT (Then) ==========
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(supplierId);
        assertThat(result.getName()).isEqualTo("Acme Corporation");
        assertThat(result.getContactInfo()).isEqualTo("contact@acme.com");
        assertThat(result.isActive()).isTrue();

        // Vérifier les appels
        verify(userRepository, times(1)).findById(adminId);
        verify(supplierRepository, times(1)).findByName("Acme Corporation");
        verify(supplierRepository, times(1)).save(any(Supplier.class));
        verify(supplierMapper, times(1)).toEntity(createRequest);
        verify(supplierMapper, times(1)).toDto(supplier);
    }

    @Test
    @DisplayName("Créer un fournisseur - Échec : Nom déjà existant")
    void createSupplier_ThrowsDuplicateResourceException_WhenNameExists() {
        // ========== ARRANGE (Given) ==========
        // Simuler que l'utilisateur est ADMIN
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));

        // Simuler qu'un fournisseur avec ce nom existe déjà
        when(supplierRepository.findByName("Acme Corporation")).thenReturn(Optional.of(supplier));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> supplierService.createSupplier(createRequest, adminId))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Supplier with name 'Acme Corporation' already exists");

        // Vérifier que save() n'a JAMAIS été appelé
        verify(userRepository, times(1)).findById(adminId);
        verify(supplierRepository, times(1)).findByName("Acme Corporation");
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Créer un fournisseur - Échec : Utilisateur non ADMIN")
    void createSupplier_ThrowsForbiddenAccessException_WhenUserIsNotAdmin() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(nonAdminId)).thenReturn(Optional.of(nonAdminUser));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> supplierService.createSupplier(createRequest, nonAdminId))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("This operation is restricted to ADMIN users");

        verify(userRepository, times(1)).findById(nonAdminId);
        verify(supplierRepository, never()).findByName(any());
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Créer un fournisseur - Échec : Utilisateur non trouvé")
    void createSupplier_ThrowsResourceNotFoundException_WhenUserNotFound() {
        // ========== ARRANGE (Given) ==========
        UUID unknownUserId = UUID.randomUUID();
        when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> supplierService.createSupplier(createRequest, unknownUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Actor not found with id: " + unknownUserId);

        verify(userRepository, times(1)).findById(unknownUserId);
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    // ============================================
    // TESTS - GET ALL SUPPLIERS
    // ============================================

    @Test
    @DisplayName("Récupérer tous les fournisseurs - Succès avec ADMIN")
    void getAllSuppliers_Success_WithAdminUser() {
        // ========== ARRANGE (Given) ==========
        UUID supplier2Id = UUID.randomUUID();
        Supplier supplier2 = new Supplier();
        supplier2.setId(supplier2Id);
        supplier2.setName("Global Supplies Inc");
        supplier2.setContactInfo("info@globalsupplies.com");
        supplier2.setActive(true);

        SupplierDto supplierDto2 = new SupplierDto();
        supplierDto2.setId(supplier2Id);
        supplierDto2.setName("Global Supplies Inc");
        supplierDto2.setContactInfo("info@globalsupplies.com");
        supplierDto2.setActive(true);

        List<Supplier> suppliers = Arrays.asList(supplier, supplier2);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(supplierRepository.findAll()).thenReturn(suppliers);
        when(supplierMapper.toDto(supplier)).thenReturn(supplierDto);
        when(supplierMapper.toDto(supplier2)).thenReturn(supplierDto2);

        // ========== ACT (When) ==========
        List<SupplierDto> results = supplierService.getAllSuppliers(adminId);

        // ========== ASSERT (Then) ==========
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("Acme Corporation");
        assertThat(results.get(1).getName()).isEqualTo("Global Supplies Inc");
        assertThat(results.get(0).getContactInfo()).isEqualTo("contact@acme.com");
        assertThat(results.get(1).getContactInfo()).isEqualTo("info@globalsupplies.com");

        verify(userRepository, times(1)).findById(adminId);
        verify(supplierRepository, times(1)).findAll();
        verify(supplierMapper, times(2)).toDto(any(Supplier.class));
    }

    @Test
    @DisplayName("Récupérer tous les fournisseurs - Échec : Utilisateur non ADMIN")
    void getAllSuppliers_ThrowsForbiddenAccessException_WhenUserIsNotAdmin() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(nonAdminId)).thenReturn(Optional.of(nonAdminUser));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> supplierService.getAllSuppliers(nonAdminId))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("This operation is restricted to ADMIN users");

        verify(userRepository, times(1)).findById(nonAdminId);
        verify(supplierRepository, never()).findAll();
    }

    @Test
    @DisplayName("Récupérer tous les fournisseurs - Liste vide")
    void getAllSuppliers_ReturnsEmptyList_WhenNoSuppliers() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(supplierRepository.findAll()).thenReturn(Arrays.asList());

        // ========== ACT (When) ==========
        List<SupplierDto> results = supplierService.getAllSuppliers(adminId);

        // ========== ASSERT (Then) ==========
        assertThat(results).isNotNull();
        assertThat(results).isEmpty();

        verify(userRepository, times(1)).findById(adminId);
        verify(supplierRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Récupérer tous les fournisseurs - Utilisateur non trouvé")
    void getAllSuppliers_ThrowsResourceNotFoundException_WhenUserNotFound() {
        // ========== ARRANGE (Given) ==========
        UUID unknownUserId = UUID.randomUUID();
        when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> supplierService.getAllSuppliers(unknownUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Actor not found with id: " + unknownUserId);

        verify(userRepository, times(1)).findById(unknownUserId);
        verify(supplierRepository, never()).findAll();
    }
}
