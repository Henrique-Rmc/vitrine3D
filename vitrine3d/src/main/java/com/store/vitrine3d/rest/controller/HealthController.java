package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.infrastructure.health.HealthCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rota pensada pra ser chamada por um pinger externo (uptime monitor / cron) em produção —
 * cada chamada aqui faz um round-trip de verdade no Postgres e no Redis, o que evita o
 * cold-start do Neon (auto-suspende sem query) e do Upstash. Sozinha essa rota não impede a
 * própria instância do Render de suspender por falta de tráfego HTTP — isso depende de algo
 * externo batendo aqui periodicamente. Ver também KeepAliveScheduler, que cobre o caso da
 * instância continuar de pé mas ociosa.
 */
@Tag(name = "Health", description = "Verificação de disponibilidade do serviço e de suas dependências (Postgres, Redis)")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthCheckService healthCheckService;

    public HealthController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @Operation(summary = "Retorna status da aplicação e de suas dependências — Postgres e Redis (público)")
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, String> components = healthCheckService.checkComponents();
        boolean allUp = components.values().stream().allMatch("UP"::equals);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", allUp ? "UP" : "DOWN");
        body.put("components", components);
        body.put("timestamp", Instant.now());

        return ResponseEntity.status(allUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
