package com.siron.authenticationservice.service.impl;

import com.siron.authenticationservice.dto.LoginResponseDto;
import com.siron.authenticationservice.exception.AuthenticationException;
import com.siron.authenticationservice.exception.ResourceAlreadyExistsException;
import com.siron.authenticationservice.model.User;
import com.siron.authenticationservice.repository.UserRepository;
import com.siron.authenticationservice.service.AuthenticationService;
import com.siron.authenticationservice.service.RedisService;
import com.siron.authenticationservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthenticationServiceImpl(RedisService redisService, UserRepository userRepository, JwtUtil jwtUtil) {
        this.redisService = redisService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * @param username username
     * @param password password
     * @return Mono<LoginResponseDto>
     */
    @Override
    public Mono<LoginResponseDto> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user ->
                        LoginResponseDto.builder()
                                .accessToken(jwtUtil.generateToken(user.getUsername()))
                                .user(user)
                                .build()
                )
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid credentials")));
    }

    /**
     * @param user user object
     * @return Mono<User>
     */
    @Override
    public Mono<User> register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.findByUsername(user.getUsername())
                .flatMap(existingUser -> Mono.<User>error(new ResourceAlreadyExistsException("User already exists")))
                .switchIfEmpty(userRepository.save(user));
    }

    /**
     * @param accessToken access token
     * @return Mono<User>
     */
    @Override
    public Mono<User> authenticateUser(String accessToken) {
        return Mono.just(accessToken)
                .filterWhen(token -> Mono.just(jwtUtil.validateToken(token)))
                .map(jwtUtil::extractUsername)
                .flatMap(this::findUserWithCache)
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid token")));
    }

    private Mono<User> findUserWithCache(String username) {
        return redisService.getCache(getCacheKey(username))
                .cast(User.class)
                .doOnSuccess(user -> {
                    if (user != null) {
                        log.info("User {} found in cache", username);
                    }
                })
                .switchIfEmpty(userRepository.findByUsername(username)
                        .flatMap(this::cacheUser));
    }

    private Mono<User> cacheUser(User user) {
        log.info("Caching user: {}", user.getUsername());
        return redisService.saveCache(getCacheKey(user.getUsername()), user)
                .cast(User.class);
    }

    private String getCacheKey(String username) {
        return "user:" + username;
    }
}
