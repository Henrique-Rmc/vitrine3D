package com.store.vitrine3d.infrastructure.health;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

// Ping de verdade em cada dependencia externa (nao so "a JVM esta de pe") — usado tanto pela
// rota publica /api/health (pra um pinger externo bater) quanto pelo KeepAliveScheduler
// interno. Postgres (Neon) e Redis (Upstash) em producao sao servicos serverless que
// suspendem/desconectam depois de um tempo sem uso — o ponto desse check e forcar um
// round-trip real em cada um.
@Component
public class HealthCheckService {

    private static final int VALIDATION_TIMEOUT_SECONDS = 2;

    private final DataSource dataSource;
    private final StringRedisTemplate redisTemplate;

    public HealthCheckService(DataSource dataSource, StringRedisTemplate redisTemplate) {
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
    }

    public Map<String, String> checkComponents() {
        Map<String, String> statuses = new LinkedHashMap<>();
        statuses.put("database", checkDatabase() ? "UP" : "DOWN");
        statuses.put("redis", checkRedis() ? "UP" : "DOWN");
        return statuses;
    }

    private boolean checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(VALIDATION_TIMEOUT_SECONDS);
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception e) {
            return false;
        }
    }
}
