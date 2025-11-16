package org.usermanagement.traceandtrust.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.usermanagement.traceandtrust.dto.CreateUserRequest;
import org.usermanagement.traceandtrust.dto.LoginRequest;
import org.usermanagement.traceandtrust.dto.UserDto;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.AuthenticationException;
import org.usermanagement.traceandtrust.exception.DuplicateResourceException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.UserMapper;
import org.usermanagement.traceandtrust.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserServiceImpl
 *
 * Méthodes testées :
 * - register(CreateUserRequest) : Inscription d'un utilisateur
 * - login(LoginRequest) : Authentification par email/password
 * - getAllUsers(UUID actorId) : Liste des utilisateurs (ADMIN seulement)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service User")
class UserServiceImplTest {

    // ============================================
    // MOCKS ET INJECTS
    // ============================================

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    // ============================================
    // DONNÉES DE TEST
    // ============================================

    private UUID adminId;
    private UUID userId;
    private UUID newUserId;
    private User adminUser;
    private User regularUser;
    private User newUser;
    private UserDto adminUserDto;
    private UserDto regularUserDto;
    private UserDto newUserDto;
    private CreateUserRequest createUserRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // IDs
        adminId = UUID.randomUUID();
        userId = UUID.randomUUID();
        newUserId = UUID.randomUUID();

        // Utilisateur ADMIN
        adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@tracetrust.com");
        adminUser.setPassword("hashedPassword123");
        adminUser.setRole(Role.ADMIN);

        adminUserDto = new UserDto();
        adminUserDto.setId(adminId);
        adminUserDto.setName("Admin User");
        adminUserDto.setEmail("admin@tracetrust.com");
        adminUserDto.setRole(Role.ADMIN);

        // Utilisateur régulier (USER)
        regularUser = new User();
        regularUser.setId(userId);
        regularUser.setName("John Doe");
        regularUser.setEmail("john@example.com");
        regularUser.setPassword("hashedPassword456");
        regularUser.setRole(Role.USER);

        regularUserDto = new UserDto();
        regularUserDto.setId(userId);
        regularUserDto.setName("John Doe");
        regularUserDto.setEmail("john@example.com");
        regularUserDto.setRole(Role.USER);

        // Nouvel utilisateur à créer
        newUser = new User();
        newUser.setId(newUserId);
        newUser.setName("New User");
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("hashedPassword789");
        newUser.setRole(Role.USER);

        newUserDto = new UserDto();
        newUserDto.setId(newUserId);
        newUserDto.setName("New User");
        newUserDto.setEmail("newuser@example.com");
        newUserDto.setRole(Role.USER);

        // Requête de création (register)
        createUserRequest = new CreateUserRequest();
        createUserRequest.setName("New User");
        createUserRequest.setEmail("newuser@example.com");
        createUserRequest.setPassword("SecurePassword123!");
        createUserRequest.setRole(Role.USER);

        // Requête de login
        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("hashedPassword456");
    }

    // ============================================
    // TESTS - REGISTER (Inscription)
    // ============================================

    @Test
    @DisplayName("Inscription d'un utilisateur - Succès")
    void register_Success() {
        // ========== ARRANGE (Given) ==========
        // L'email n'existe pas encore
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(any(CreateUserRequest.class))).thenReturn(newUser);
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userMapper.toDto(any(User.class))).thenReturn(newUserDto);

        // ========== ACT (When) ==========
        UserDto result = userService.register(createUserRequest);

        // ========== ASSERT (Then) ==========
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(newUserId);
        assertThat(result.getName()).isEqualTo("New User");
        assertThat(result.getEmail()).isEqualTo("newuser@example.com");
        assertThat(result.getRole()).isEqualTo(Role.USER);

        // Vérifier les appels
        verify(userRepository, times(1)).findByEmail("newuser@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toEntity(createUserRequest);
        verify(userMapper, times(1)).toDto(newUser);
    }

    @Test
    @DisplayName("Inscription d'un utilisateur - Échec : Email déjà existant")
    void register_ThrowsDuplicateResourceException_WhenEmailExists() {
        // ========== ARRANGE (Given) ==========
        // L'email existe déjà
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(regularUser));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> userService.register(createUserRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already in use");

        // Vérifier que save() n'a JAMAIS été appelé
        verify(userRepository, times(1)).findByEmail("newuser@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    // ============================================
    // TESTS - LOGIN (Authentification)
    // ============================================

    @Test
    @DisplayName("Connexion - Succès")
    void login_Success() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(regularUser));
        when(userMapper.toDto(regularUser)).thenReturn(regularUserDto);

        // ========== ACT (When) ==========
        UserDto result = userService.login(loginRequest);

        // ========== ASSERT (Then) ==========
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getRole()).isEqualTo(Role.USER);

        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(userMapper, times(1)).toDto(regularUser);
    }

    @Test
    @DisplayName("Connexion - Échec : Email non trouvé")
    void login_ThrowsResourceNotFoundException_WhenEmailNotFound() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        LoginRequest badRequest = new LoginRequest();
        badRequest.setEmail("unknown@example.com");
        badRequest.setPassword("password");

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> userService.login(badRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User with email");

        verify(userRepository, times(1)).findByEmail("unknown@example.com");
    }

    @Test
    @DisplayName("Connexion - Échec : Mot de passe incorrect")
    void login_ThrowsAuthenticationException_WhenPasswordIncorrect() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(regularUser));

        LoginRequest badRequest = new LoginRequest();
        badRequest.setEmail("john@example.com");
        badRequest.setPassword("WrongPassword!");  // Mauvais mot de passe

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> userService.login(badRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid password");

        verify(userRepository, times(1)).findByEmail("john@example.com");
    }

    // ============================================
    // TESTS - GET ALL USERS
    // ============================================

    @Test
    @DisplayName("Récupérer tous les utilisateurs - Succès avec ADMIN")
    void getAllUsers_Success_WithAdminUser() {
        // ========== ARRANGE (Given) ==========
        List<User> users = Arrays.asList(adminUser, regularUser);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDto(adminUser)).thenReturn(adminUserDto);
        when(userMapper.toDto(regularUser)).thenReturn(regularUserDto);

        // ========== ACT (When) ==========
        List<UserDto> results = userService.getAllUsers(adminId);

        // ========== ASSERT (Then) ==========
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("Admin User");
        assertThat(results.get(1).getName()).isEqualTo("John Doe");
        assertThat(results.get(0).getRole()).isEqualTo(Role.ADMIN);
        assertThat(results.get(1).getRole()).isEqualTo(Role.USER);

        verify(userRepository, times(1)).findById(adminId);
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(2)).toDto(any(User.class));
    }

    @Test
    @DisplayName("Récupérer tous les utilisateurs - Échec : Utilisateur non ADMIN")
    void getAllUsers_ThrowsForbiddenAccessException_WhenUserIsNotAdmin() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> userService.getAllUsers(userId))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("ADMIN");

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).findAll();
    }

    @Test
    @DisplayName("Récupérer tous les utilisateurs - Liste vide")
    void getAllUsers_ReturnsEmptyList_WhenNoUsers() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // ========== ACT (When) ==========
        List<UserDto> results = userService.getAllUsers(adminId);

        // ========== ASSERT (Then) ==========
        assertThat(results).isNotNull();
        assertThat(results).isEmpty();

        verify(userRepository, times(1)).findById(adminId);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Récupérer tous les utilisateurs - Échec : Acteur non trouvé")
    void getAllUsers_ThrowsResourceNotFoundException_WhenActorNotFound() {
        // ========== ARRANGE (Given) ==========
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> userService.getAllUsers(unknownId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Actor not found");

        verify(userRepository, times(1)).findById(unknownId);
        verify(userRepository, never()).findAll();
    }
}