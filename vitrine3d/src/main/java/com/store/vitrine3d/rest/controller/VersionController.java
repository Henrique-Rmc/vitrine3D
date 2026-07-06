package com.store.vitrine3d.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Confirma qual commit está de fato rodando em produção, sem precisar supor a partir de
 * comportamento observado. O commit vem de GIT_COMMIT, que é passado como build-arg
 * (capturado com "git rev-parse --short HEAD" na hora do docker build) — não depende de
 * git dentro do container nem de nenhum arquivo de resource lido em runtime.
 */
@Tag(name = "Version", description = "Identifica a versão/commit em execução")
@RestController
@RequestMapping("/api/version")
public class VersionController {

    @Value("${app.version.commit:unknown}")
    private String commit;

    private final Instant startedAt = Instant.now();

    @Operation(summary = "Retorna o commit em execução e há quanto tempo a instância está de pé (público)")
    @GetMapping
    public ResponseEntity<Map<String, Object>> version() {
        return ResponseEntity.ok(Map.of(
                "commit", commit,
                "startedAt", startedAt
        ));
    }
}
