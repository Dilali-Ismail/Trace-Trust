package org.usermanagement.traceandtrust.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.CreateUserRequest;
import org.usermanagement.traceandtrust.dto.LoginRequest;
import org.usermanagement.traceandtrust.dto.UserDto;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.exeception.AuthenticationException;
import org.usermanagement.traceandtrust.exeception.DuplicateResourceException;
import org.usermanagement.traceandtrust.exeception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.UserMapper;
import org.usermanagement.traceandtrust.repository.UserRepository;

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
    public UserDto login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User with email  not found."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new AuthenticationException("Invalid password.");
        }

        return userMapper.toDto(user);


    }
}