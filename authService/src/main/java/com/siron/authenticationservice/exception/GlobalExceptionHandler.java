package com.siron.authenticationservice.exception;

import com.siron.authenticationservice.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponseDto<Object>>> handleValidationErrors(WebExchangeBindException ex) {
        String errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("Validation error: {}", errors);

        return Mono.just(ResponseEntity.badRequest()
                .body(ApiResponseDto.builder()
                        .status("error")
                        .message(errors)
                        .build()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<ApiResponseDto<Object>>> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.builder()
                        .status("error")
                        .message(ex.getMessage())
                        .build()));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public Mono<ResponseEntity<ApiResponseDto<Object>>> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        log.error("Resource Already Exists error: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.builder()
                        .status("error")
                        .message(ex.getMessage())
                        .build()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<ResponseEntity<ApiResponseDto<Object>>> handleResourceNotFoundException(NoResourceFoundException ex) {
        log.error("Resource not found", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.builder()
                        .status("error")
                        .message("Resource not found")
                        .build()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponseDto<Object>>> handleUnhandledException(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.builder()
                        .status("error")
                        .message("Internal Server Error")
                        .build()));
    }
}