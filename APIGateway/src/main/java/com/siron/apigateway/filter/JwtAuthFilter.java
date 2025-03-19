package com.siron.apigateway.filter;

import com.siron.apigateway.dto.ApiResponseDto;
import com.siron.apigateway.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    @Autowired
    public JwtAuthFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Skip authentication for auth service endpoints
            if (request.getURI().getPath().startsWith("/api/auth")) {
                return chain.filter(exchange);
            }

            // Check if the request has an Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);
            if (!authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.replace("Bearer ", "");

            // Call auth service to validate token
            return webClientBuilder.build()
                    .get()
                    .uri(request.getURI().getScheme() + "://" + request.getURI().getHost() + ":" + request.getURI().getPort() + "/api/auth/authenticate")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<UserDto>>() {
                    })
                    .flatMap(response -> {
                        Boolean isValid = response.getStatus().equals("success");
                        UserDto user = response.getData();
                        if (Boolean.TRUE.equals(isValid) && user != null) {
                            // Add user info as headers for downstream services
                            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                    .header("X-User-Id", user.getUsername())
                                    .header("X-User-Role", user.getRole())
                                    .build();

                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        }

                        return onError(exchange, "Unauthorized", HttpStatus.UNAUTHORIZED);
                    })
                    .onErrorResume(error -> onError(exchange, "Unauthorized", HttpStatus.UNAUTHORIZED));
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            String errorPayload = String.format("{\"status\": \"error\", \"message\": \"%s\"}", message);
            return bufferFactory.wrap(errorPayload.getBytes(StandardCharsets.UTF_8));
        }));
    }

    public static class Config {
        // Configuration properties if needed
    }
}