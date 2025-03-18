package com.siron.authenticationservice.service;

import reactor.core.publisher.Mono;

public interface RedisService {

    public Mono<Object> saveCache(String key, Object value);
    public Mono<Object> getCache(String key);
}
