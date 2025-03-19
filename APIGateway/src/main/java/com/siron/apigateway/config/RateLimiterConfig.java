package com.siron.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver serviceBasedKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getURI().getPath();
            String[] parts = path.split("/");

            // Extract service name from "/api/serviceA/**"
            String servicePrefix = (parts.length > 2) ? parts[2] : "default";

            // Extract Client ID or fallback to IP
            String clientId = exchange.getRequest().getHeaders().getFirst("X-Client-Id");
            String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
            String key = servicePrefix + ":" + ((clientId != null && !clientId.isEmpty()) ? clientId : ip);

            return Mono.just(key);
        };
    }
}
