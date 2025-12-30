package org.usermanagement.traceandtrust.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CreateUserRequest;
import org.usermanagement.traceandtrust.dto.LoginRequest;
import org.usermanagement.traceandtrust.dto.UserDto;
import org.usermanagement.traceandtrust.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody CreateUserRequest request) {
        UserDto createdUser = userService.register(request);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    /*
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody LoginRequest request) {
        UserDto user = userService.login(request);
        return ResponseEntity.ok(user);
    }
    */

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUser(@RequestHeader("X-Request-id") UUID actorId){
        List<UserDto> allUsers = userService.getAllUsers(actorId);
        return ResponseEntity.ok(allUsers);
    }

}
