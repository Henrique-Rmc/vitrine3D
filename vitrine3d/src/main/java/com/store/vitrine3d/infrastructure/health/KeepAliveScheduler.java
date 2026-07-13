package com.store.vitrine3d.infrastructure.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Ping periodico em Postgres e Redis enquanto a instancia da aplicacao estiver de pe —
 * mantem o compute do Neon (auto-suspende por padrao depois de ~5min sem query) e a conexao
 * do Upstash ativos. Isso NAO substitui um pinger externo batendo em /api/health: se a
 * propria instancia (Render) suspender por falta de trafego HTTP, esse scheduler para de
 * rodar junto com ela — cobre só o caso da instância continuar de pé mas ociosa.
 */
@Component
@ConditionalOnProperty(name = "app.keep-alive.enabled", havingValue = "true", matchIfMissing = true)
public class KeepAliveScheduler {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveScheduler.class);

    private final HealthCheckService healthCheckService;

    public KeepAliveScheduler(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @Scheduled(initialDelayString = "${app.keep-alive.initial-delay-ms:60000}",
               fixedDelayString = "${app.keep-alive.interval-ms:240000}")
    public void keepAlive() {
        Map<String, String> statuses = healthCheckService.checkComponents();
        statuses.forEach((component, status) -> {
            if (!"UP".equals(status)) {
                log.warn("Keep-alive check failed for {}: {}", component, status);
            }
        });
    }
}
