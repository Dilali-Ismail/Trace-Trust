package org.usermanagement.traceandtrust.service;

import jdk.jfr.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.usermanagement.traceandtrust.dto.CarrierDto;
import org.usermanagement.traceandtrust.dto.CreateCarrierRequest;
import org.usermanagement.traceandtrust.entity.Carrier;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.DuplicateResourceException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.CarrierMapper;
import org.usermanagement.traceandtrust.repository.CarrierRepository;
import org.usermanagement.traceandtrust.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Name("Test pour Carrier Service")
public class CarrierServiceImplTest {

    @Mock
    private CarrierRepository carrierRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarrierMapper carrierMapper;

    @InjectMocks
    private CarrierServiceImpl carrierService;

    // ============================================
    // DONNÉES DE TEST
    // ============================================

    private UUID adminId;
    private UUID nonAdminId;
    private UUID carrierId;
    private User adminUser;
    private User nonAdminUser;
    private Carrier carrier;
    private CarrierDto carrierDto;
    private CreateCarrierRequest createRequest;

    @BeforeEach
    void setUp() {
        // IDs
        adminId = UUID.randomUUID();
        nonAdminId = UUID.randomUUID();
        carrierId = UUID.randomUUID();

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

        // Créer un Carrier (entité)
        carrier = new Carrier();
        carrier.setId(carrierId);
        carrier.setName("DHL Express");
        carrier.setActive(true);

        // Créer un CarrierDto
        carrierDto = new CarrierDto();
        carrierDto.setId(carrierId);
        carrierDto.setName("DHL Express");
        carrierDto.setActive(true);

        // Créer une requête de création
        createRequest = new CreateCarrierRequest();
        createRequest.setName("DHL Express");
    }

    // ============================================
    // TESTS - CREATE CARRIER
    // ============================================

    @Test
    @DisplayName("Créer un transporteur - Succès avec utilisateur ADMIN")
    void createCarrier_Success_WithAdminUser() {
        // ========== ARRANGE (Given) ==========
        // Simuler que l'utilisateur est ADMIN
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));

        // Simuler que le mapper convertit la requête en entité
        when(carrierMapper.toEntity(any(CreateCarrierRequest.class))).thenReturn(carrier);

        // Simuler que le repository sauvegarde le transporteur
        when(carrierRepository.save(any(Carrier.class))).thenReturn(carrier);

        // Simuler que le mapper convertit l'entité en DTO
        when(carrierMapper.toDto(any(Carrier.class))).thenReturn(carrierDto);

        // ========== ACT (When) ==========
        CarrierDto result = carrierService.createCarrier(createRequest);

        // ========== ASSERT (Then) ==========
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(carrierId);
        assertThat(result.getName()).isEqualTo("DHL Express");
        assertThat(result.isActive()).isTrue();

        // Vérifier les appels
        verify(userRepository, times(1)).findById(adminId);
        verify(carrierRepository, times(1)).save(any(Carrier.class));
        verify(carrierMapper, times(1)).toEntity(createRequest);
        verify(carrierMapper, times(1)).toDto(carrier);
    }

    @Test
    @DisplayName("Créer un transporteur - Échec : Utilisateur non ADMIN")
    void createCarrier_ThrowsForbiddenAccessException_WhenUserIsNotAdmin() {
        // ========== ARRANGE (Given) ==========
        // Simuler que l'utilisateur n'est PAS ADMIN
        when(userRepository.findById(nonAdminId)).thenReturn(Optional.of(nonAdminUser));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> carrierService.createCarrier(createRequest))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("This operation is restricted to ADMIN users");

        // Vérifier que save() n'a JAMAIS été appelé
        verify(userRepository, times(1)).findById(nonAdminId);
        verify(carrierRepository, never()).save(any(Carrier.class));
    }

    @Test
    @DisplayName("Créer un transporteur - Échec : Utilisateur non trouvé")
    void createCarrier_ThrowsResourceNotFoundException_WhenUserNotFound() {
        // ========== ARRANGE (Given) ==========
        UUID unknownUserId = UUID.randomUUID();
        when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> carrierService.createCarrier(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Actor not found with id: " + unknownUserId);

        verify(userRepository, times(1)).findById(unknownUserId);
        verify(carrierRepository, never()).save(any(Carrier.class));
    }

    // ============================================
    // TESTS - GET ALL CARRIERS
    // ============================================

    @Test
    @DisplayName("Récupérer tous les transporteurs - Succès avec ADMIN")
    void getAllCarriers_Success_WithAdminUser() {
        // ========== ARRANGE (Given) ==========
        UUID carrier2Id = UUID.randomUUID();
        Carrier carrier2 = new Carrier();
        carrier2.setId(carrier2Id);
        carrier2.setName("FedEx");
        carrier2.setActive(true);

        CarrierDto carrierDto2 = new CarrierDto();
        carrierDto2.setId(carrier2Id);
        carrierDto2.setName("FedEx");
        carrierDto2.setActive(true);

        List<Carrier> carriers = Arrays.asList(carrier, carrier2);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(carrierRepository.findAll()).thenReturn(carriers);
        when(carrierMapper.toDto(carrier)).thenReturn(carrierDto);
        when(carrierMapper.toDto(carrier2)).thenReturn(carrierDto2);

        // ========== ACT (When) ==========
        List<CarrierDto> results = carrierService.getAllCarriers();

        // ========== ASSERT (Then) ==========
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("DHL Express");
        assertThat(results.get(1).getName()).isEqualTo("FedEx");
        assertThat(results.get(0).isActive()).isTrue();
        assertThat(results.get(1).isActive()).isTrue();

        verify(userRepository, times(1)).findById(adminId);
        verify(carrierRepository, times(1)).findAll();
        verify(carrierMapper, times(2)).toDto(any(Carrier.class));
    }

    @Test
    @DisplayName("Récupérer tous les transporteurs - Échec : Utilisateur non ADMIN")
    void getAllCarriers_ThrowsForbiddenAccessException_WhenUserIsNotAdmin() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(nonAdminId)).thenReturn(Optional.of(nonAdminUser));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> carrierService.getAllCarriers())
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("This operation is restricted to ADMIN users");

        verify(userRepository, times(1)).findById(nonAdminId);
        verify(carrierRepository, never()).findAll();
    }

    @Test
    @DisplayName("Récupérer tous les transporteurs - Liste vide")
    void getAllCarriers_ReturnsEmptyList_WhenNoCarriers() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(carrierRepository.findAll()).thenReturn(Arrays.asList());

        // ========== ACT (When) ==========
        List<CarrierDto> results = carrierService.getAllCarriers();

        // ========== ASSERT (Then) ==========
        assertThat(results).isNotNull();
        assertThat(results).isEmpty();

        verify(userRepository, times(1)).findById(adminId);
        verify(carrierRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Récupérer tous les transporteurs - Utilisateur non trouvé")
    void getAllCarriers_ThrowsResourceNotFoundException_WhenUserNotFound() {
        // ========== ARRANGE (Given) ==========
        UUID unknownUserId = UUID.randomUUID();
        when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> carrierService.getAllCarriers())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Actor not found with id: " + unknownUserId);

        verify(userRepository, times(1)).findById(unknownUserId);
        verify(carrierRepository, never()).findAll();
    }
}