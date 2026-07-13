package com.store.vitrine3d.infrastructure.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @Mock private DataSource dataSource;
    @Mock private Connection connection;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private RedisConnectionFactory redisConnectionFactory;
    @Mock private RedisConnection redisConnection;

    private HealthCheckService healthCheckService;

    private void setUp() {
        healthCheckService = new HealthCheckService(dataSource, redisTemplate);
    }

    @Test
    void checkComponents_allHealthy_returnsUp() throws SQLException {
        setUp();
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);
        when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        Map<String, String> result = healthCheckService.checkComponents();

        assertThat(result).containsEntry("database", "UP").containsEntry("redis", "UP");
    }

    @Test
    void checkComponents_databaseConnectionThrows_returnsDatabaseDown() throws SQLException {
        setUp();
        when(dataSource.getConnection()).thenThrow(new SQLException("connection refused"));
        when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        Map<String, String> result = healthCheckService.checkComponents();

        assertThat(result).containsEntry("database", "DOWN").containsEntry("redis", "UP");
    }

    @Test
    void checkComponents_databaseInvalid_returnsDatabaseDown() throws SQLException {
        setUp();
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(false);
        when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        Map<String, String> result = healthCheckService.checkComponents();

        assertThat(result).containsEntry("database", "DOWN");
    }

    @Test
    void checkComponents_redisThrows_returnsRedisDown() throws SQLException {
        setUp();
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);
        when(redisTemplate.getConnectionFactory()).thenThrow(new RuntimeException("redis unavailable"));

        Map<String, String> result = healthCheckService.checkComponents();

        assertThat(result).containsEntry("database", "UP").containsEntry("redis", "DOWN");
    }

    @Test
    void checkComponents_redisPingNotPong_returnsRedisDown() throws SQLException {
        setUp();
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);
        when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn(null);

        Map<String, String> result = healthCheckService.checkComponents();

        assertThat(result).containsEntry("redis", "DOWN");
    }
}
