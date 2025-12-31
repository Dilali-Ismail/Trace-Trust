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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

//    @Override
//    public UserDto register(CreateUserRequest request) {
//        userRepository.findByEmail(request.getEmail()).ifPresent(user ->
//        {
//            throw new DuplicateResourceException("Email already in use");
//        });
//
//        User user = userMapper.toEntity(request);
//        User saveUser = userRepository.save(user);
//        return userMapper.toDto(saveUser);
//    }
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
        String keycloakId = jwt.getSubject();
        String email = jwt.getClaim("email");

        String firstName = jwt.getClaim("given_name");
        String lastName = jwt.getClaim("family_name");
        String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        fullName = fullName.trim();

        if (fullName.isEmpty()) {
            fullName = email;
        }

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> roles = (List<String>) realmAccess.get("roles");

        Role roleToSave = Role.CLIENT;
        if(roles.contains("ADMIN")){
            roleToSave = Role.ADMIN;
        } else if (roles.contains("WAREHOUSE_MANAGER")){
            roleToSave = Role.WAREHOUSE_MANAGER;
        }

        final Role finalRole = roleToSave;
        final String finalName = fullName;

        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    boolean changed = false;

                    if(!keycloakId.equals(existingUser.getKeycloakId())) {
                        existingUser.setKeycloakId(keycloakId);
                        changed = true;
                    }

                    if(!existingUser.getRole().equals(finalRole)){
                        existingUser.setRole(finalRole);
                        changed = true;
                    }

                    return changed ? userRepository.save(existingUser) : existingUser;
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setKeycloakId(keycloakId);
                    newUser.setEmail(email);
                    newUser.setName(finalName);
                    newUser.setActive(true);
                    newUser.setRole(finalRole);
                    return userRepository.save(newUser);
                });
    }
}