package org.usermanagement.traceandtrust.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto register(CreateUserRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user ->
        {
            throw new DuplicateResourceException("Email already in use");
        });

        User user = userMapper.toEntity(request);
        User saveUser = userRepository.save(user);
        return userMapper.toDto(saveUser);
    }
/*
    public UserDto login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User with email  not found."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new AuthenticationException("Invalid password.");
        }

        return userMapper.toDto(user);

    }
*/

    public List<UserDto> getAllUsers(UUID actorId){
        User actor = userRepository.findById(actorId).orElseThrow(()-> new ResourceNotFoundException("Actor not found"));

        if(!actor.getRole().equals(Role.ADMIN)){
            throw new ForbiddenAccessException("Only an ADMIN can view the list of all users.");
        }
        return userRepository.findAll().stream().map(userMapper::toDto).collect(Collectors.toList());
    }
    public User syncUser(Jwt jwt) {
        // 1. Récupérer l'ID unique Keycloak (le plus fiable)
        String keycloakId = jwt.getSubject();

        // 2. Récupérer les infos utiles (Email, Nom...)
        String email = jwt.getClaim("email");

        // 3. Chercher par ID Keycloak (ou Email pour la migration)
        return userRepository.findByEmail(email) // Ou findByKeycloakId(keycloakId)
                .map(existingUser -> {
                    // Mise à jour optionnelle : si l'utilisateur a changé de nom dans Keycloak
                    if (!existingUser.getKeycloakId().equals(keycloakId)) {
                        existingUser.setKeycloakId(keycloakId);
                        return userRepository.save(existingUser);
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    // 4. Création à la volée ("Onboarding")
                    User newUser = new User();
                    newUser.setKeycloakId(keycloakId);
                    newUser.setEmail(email);
                    newUser.setActive(true);
                    newUser.setRole(Role.CLIENT); // Rôle par défaut, ou extrait du JWT
                    return userRepository.save(newUser);
                });
    }
}