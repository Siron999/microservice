package com.siron.authenticationservice.service.impl;

import com.siron.authenticationservice.service.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class RedisServiceImpl implements RedisService {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final Duration cacheTtl;

    public RedisServiceImpl(
            ReactiveRedisTemplate<String, Object> redisTemplate,
            @Value("${redis.cache.ttl-hours}") long ttlHours) {
        this.redisTemplate = redisTemplate;
        this.cacheTtl = Duration.ofHours(ttlHours);
    }

    @Override
    public Mono<Object> saveCache(String key, Object value) {
        return redisTemplate.opsForValue().set(key, value, cacheTtl).thenReturn(value);
    }

    @Override
    public Mono<Object> getCache(String key) {
        return redisTemplate.opsForValue().get(key)
                .switchIfEmpty(Mono.empty());
    }
}
