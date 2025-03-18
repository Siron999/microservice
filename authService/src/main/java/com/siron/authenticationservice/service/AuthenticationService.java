package com.siron.authenticationservice.service;

import com.siron.authenticationservice.dto.LoginResponseDto;
import com.siron.authenticationservice.model.User;
import reactor.core.publisher.Mono;

public interface AuthenticationService {
    public Mono<LoginResponseDto> login(String username, String password);

    public Mono<User> register(User user);

    public Mono<User> authenticateUser(String accessToken);
}
