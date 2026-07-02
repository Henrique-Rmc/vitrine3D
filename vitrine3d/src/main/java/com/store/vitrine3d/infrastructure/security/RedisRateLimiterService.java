package com.store.vitrine3d.infrastructure.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
public class RedisRateLimiterService {

    private final StringRedisTemplate redis;

    public RedisRateLimiterService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean tryConsume(String key, int limit, Duration window) {
        Long count = redis.opsForValue().increment(key);
        if (Long.valueOf(1L).equals(count)) {
            redis.expire(key, window);
        }
        return count != null && count <= limit;
    }
}
