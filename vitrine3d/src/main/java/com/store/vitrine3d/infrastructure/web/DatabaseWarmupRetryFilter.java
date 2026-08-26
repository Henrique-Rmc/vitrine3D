package com.store.vitrine3d.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Reexecuta a requisicao inteira quando ela falha por causa do compute do Neon estar suspenso
 * (ver GlobalExceptionHandler.handleDatabaseWarmingUp) — sem isso, a primeira requisicao depois
 * de um periodo ocioso falha com erro em vez de so demorar um pouco mais.
 *
 * So e seguro reexecutar porque CannotCreateTransactionException/DataAccessResourceFailureException
 * acontecem na ABERTURA da conexao, antes de qualquer service/repository rodar — ou seja, a
 * tentativa que falhou garantidamente nao teve efeito colateral nenhum (nem gravou nada, nem
 * chamou storage externo). @Order(HIGHEST_PRECEDENCE) garante que esse filtro envolve a cadeia
 * inteira, inclusive Spring Security — se o JWT filter algum dia tocar o banco, tambem e coberto.
 *
 * ContentCachingResponseWrapper segura a resposta em memoria em vez de mandar pro cliente —
 * assim, numa tentativa que falha, simplesmente descartamos o que foi bufferizado e tentamos de
 * novo, sem ter mandado nenhum byte de verdade ainda.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseWarmupRetryFilter extends OncePerRequestFilter {

    private static final String RETRYABLE_CODE = "\"code\":\"DATABASE_WARMING_UP\"";

    private final long[] backoffMs;

    public DatabaseWarmupRetryFilter() {
        this(new long[] {500, 1500});
    }

    /** Visivel a testes pra nao depender de Thread.sleep real com os valores de producao. */
    DatabaseWarmupRetryFilter(long[] backoffMs) {
        this.backoffMs = backoffMs;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Multipart requests cannot be retried — Tomcat reads the underlying CoyoteInputStream
        // directly for part parsing, bypassing any wrapper's getInputStream(). Caching the body
        // here would close that stream before Tomcat gets to it, causing "Stream closed".
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith("multipart/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        int maxAttempts = backoffMs.length + 1;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
            chain.doFilter(cachedRequest, wrappedResponse);

            boolean lastAttempt = attempt == maxAttempts;
            if (lastAttempt || !isDatabaseWarmingUp(wrappedResponse)) {
                wrappedResponse.copyBodyToResponse();
                return;
            }

            response.reset();
            sleepQuietly(backoffMs[attempt - 1]);
        }
    }

    private boolean isDatabaseWarmingUp(ContentCachingResponseWrapper wrappedResponse) {
        if (wrappedResponse.getStatus() != HttpServletResponse.SC_SERVICE_UNAVAILABLE) {
            return false;
        }
        String body = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
        return body.contains(RETRYABLE_CODE);
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
