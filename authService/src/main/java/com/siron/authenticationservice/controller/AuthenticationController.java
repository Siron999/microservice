package com.siron.authenticationservice.controller;

import com.siron.authenticationservice.dto.ApiResponseDto;
import com.siron.authenticationservice.dto.LoginRequestDto;
import com.siron.authenticationservice.dto.LoginResponseDto;
import com.siron.authenticationservice.model.User;
import com.siron.authenticationservice.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
@Slf4j
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponseDto<LoginResponseDto>>> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        return authenticationService.login(loginRequest.getUsername(), loginRequest.getPassword())
                .map(response -> ResponseEntity.ok(ApiResponseDto.<LoginResponseDto>builder()
                        .status("success")
                        .data(response)
                        .build()));
    }

    @Operation(summary = "Register user", description = "Create a new user account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "User already exists")
    })
    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResponseDto<User>>> register(@Valid @RequestBody User user) {
        return authenticationService.register(user)
                .map(response -> ResponseEntity.ok(ApiResponseDto.<User>builder()
                        .status("success")
                        .data(response)
                        .build()));
    }

    @Operation(summary = "Authenticate User", description = "Validate JWT token and return user details")
    @SecurityRequirement(name = "bearer-token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @GetMapping("/authenticate")
    public Mono<ResponseEntity<ApiResponseDto<User>>> validateToken(@RequestHeader(value = "Authorization",required = false) String token) {
        if(token == null || !token.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(ApiResponseDto.<User>builder()
                            .status("error")
                            .message("Invalid token")
                            .build()));
        }
        return authenticationService.authenticateUser(token.replace("Bearer ", ""))
                .map(user -> ResponseEntity.ok(ApiResponseDto.<User>builder()
                        .status("success")
                        .data(user)
                        .build()));
    }
}