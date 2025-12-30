package org.usermanagement.traceandtrust.service;

import org.apache.catalina.LifecycleState;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.oauth2.jwt.Jwt;
import org.usermanagement.traceandtrust.dto.*;
import org.usermanagement.traceandtrust.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDto register(CreateUserRequest request);
    //UserDto login(LoginRequest request);
    List<UserDto> getAllUsers(UUID actorId);
    User syncUser(Jwt jwt);

}
