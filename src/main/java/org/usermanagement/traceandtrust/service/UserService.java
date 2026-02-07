package org.usermanagement.traceandtrust.service;

import org.apache.catalina.LifecycleState;
import org.usermanagement.traceandtrust.dto.*;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDto register(CreateUserRequest request);
    UserDto login(LoginRequest request);
    List<UserDto> getAllUsers();
    UserDto getUserById(UUID id);
    UserDto updateUser(UUID id, UpdateUserRequest request);
    UserDto toggleUserStatus(UUID id);
    void deleteUser(UUID id);
}
