package com.store.vitrine3d.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
public class RedisRateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiterService.class);

    private final StringRedisTemplate redis;

    public RedisRateLimiterService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Falha aberta (permite a requisição) se o Redis estiver indisponível — rate limiting é
     * proteção auxiliar contra abuso, nunca pode ser um ponto único de falha para login/cadastro.
     */
    public boolean tryConsume(String key, int limit, Duration window) {
        try {
            Long count = redis.opsForValue().increment(key);
            if (Long.valueOf(1L).equals(count)) {
                redis.expire(key, window);
            }
            return count != null && count <= limit;
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis indisponível para rate limiting (key='{}') — permitindo requisição", key, e);
            return true;
        }
    }
}
