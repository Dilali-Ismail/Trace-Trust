package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateUserRequest;
import org.usermanagement.traceandtrust.dto.LoginRequest;
import org.usermanagement.traceandtrust.dto.UserDto;

public interface UserService {

    UserDto register(CreateUserRequest request);
    UserDto login(LoginRequest request);
}
