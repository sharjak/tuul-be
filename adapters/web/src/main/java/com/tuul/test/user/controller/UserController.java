package com.tuul.test.user.controller;

import com.tuul.test.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Controller", description = "API for user activities")
@RestController
@RequestMapping("user")
@RequiredArgsConstructor
class UserController {
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    @Operation(summary = "Register a new user", description = "Creates a new user and returns the user with an ID.")
    @PostMapping
    ResponseEntity<UserDto> registerUser(@RequestBody @Valid SaveUserDto saveUserDto) {
        var user = userDtoMapper.toDomain(saveUserDto);
        user = userService.registerUser(user);
        var userDto = userDtoMapper.toDto(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @Operation(summary = "User Login", description = "Authenticates a user and returns a JWT token.")
    @PostMapping("/login")
    ResponseEntity<TokenDto> loginUser(@RequestBody @Valid LoginUserDto loginUserDto) {
        var token = userService.authenticateUser(loginUserDto.email(), loginUserDto.password());
        var dto = userDtoMapper.toDto(token);
        return ResponseEntity.ok(dto);
    }
}
