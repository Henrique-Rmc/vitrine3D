package com.store.vitrine3d.infrastructure.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeepAliveSchedulerTest {

    @Mock private HealthCheckService healthCheckService;

    @Test
    void keepAlive_allUp_doesNotThrow() {
        when(healthCheckService.checkComponents()).thenReturn(Map.of("database", "UP", "redis", "UP"));
        KeepAliveScheduler scheduler = new KeepAliveScheduler(healthCheckService);

        assertThatCode(scheduler::keepAlive).doesNotThrowAnyException();
        verify(healthCheckService).checkComponents();
    }

    @Test
    void keepAlive_componentDown_doesNotThrow() {
        when(healthCheckService.checkComponents()).thenReturn(Map.of("database", "DOWN", "redis", "UP"));
        KeepAliveScheduler scheduler = new KeepAliveScheduler(healthCheckService);

        assertThatCode(scheduler::keepAlive).doesNotThrowAnyException();
    }
}
