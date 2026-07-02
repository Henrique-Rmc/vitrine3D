package com.store.vitrine3d.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.vitrine3d.rest.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LOGIN_CAPACITY    = 5;
    private static final int REGISTER_CAPACITY = 3;
    private static final Duration WINDOW       = Duration.ofMinutes(1);

    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final Optional<RedisRateLimiterService> rateLimiter;

    public RateLimitFilter(ObjectMapper objectMapper,
                           @Value("${app.rate-limit.enabled:true}") boolean enabled,
                           Optional<RedisRateLimiterService> rateLimiter) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (!enabled || rateLimiter.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        String key = resolveKey(request);
        if (key != null) {
            int limit = key.startsWith("rate:login:") ? LOGIN_CAPACITY : REGISTER_CAPACITY;
            if (!rateLimiter.get().tryConsume(key, limit, WINDOW)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setHeader("Retry-After", String.valueOf(WINDOW.toSeconds()));
                objectMapper.writeValue(response.getWriter(),
                        ErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED",
                                "Too many requests. Please try again in " + WINDOW.toSeconds() + " seconds."));
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String resolveKey(HttpServletRequest request) {
        String method = request.getMethod();
        String path   = request.getRequestURI();
        String ip     = extractIp(request);

        if ("POST".equalsIgnoreCase(method) && "/api/auth/login".equals(path)) {
            return "rate:login:" + ip;
        }
        if ("POST".equalsIgnoreCase(method) && "/api/users/register".equals(path)) {
            return "rate:register:" + ip;
        }
        return null;
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
