package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.RefreshToken;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.RefreshTokenRepository;
import com.store.vitrine3d.infrastructure.config.RefreshTokenProperties;
import com.store.vitrine3d.rest.exception.InvalidRefreshTokenException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    static final String COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/api/auth";

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenProperties props;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               RefreshTokenProperties props) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.props = props;
    }

    @Transactional
    public String createFor(Store store) {
        String rawToken = UUID.randomUUID().toString();
        RefreshToken rt = new RefreshToken();
        rt.setTokenHash(sha256(rawToken));
        rt.setStore(store);
        rt.setExpiresAt(Instant.now().plus(props.getExpirationDays(), ChronoUnit.DAYS));
        refreshTokenRepository.save(rt);
        return rawToken;
    }

    /**
     * Validates the raw token and revokes it. Returns the owner Store so the caller
     * can generate a new access token and a new refresh token via {@link #createFor(Store)}.
     *
     * Reuse detection: if a revoked token is presented again, all sessions for that
     * store are immediately revoked (indicates likely token theft).
     */
    @Transactional
    public Store validateAndRevoke(String rawToken) {
        RefreshToken rt = refreshTokenRepository.findByTokenHash(sha256(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (rt.isRevoked()) {
            log.warn("Refresh token reuse detected for store '{}' — revoking all sessions",
                    rt.getStore().getEmail());
            refreshTokenRepository.revokeAllByStoreId(rt.getStore().getId());
            throw new InvalidRefreshTokenException(
                    "Refresh token already used. All sessions revoked for security.");
        }

        if (rt.getExpiresAt().isBefore(Instant.now())) {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            throw new InvalidRefreshTokenException("Session expired. Please log in again.");
        }

        rt.setRevoked(true);
        refreshTokenRepository.save(rt);
        return rt.getStore();
    }

    @Transactional
    public void revokeByRawToken(String rawToken) {
        refreshTokenRepository.findByTokenHash(sha256(rawToken)).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    public String extractFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public ResponseCookie buildCookie(String rawToken) {
        return ResponseCookie.from(COOKIE_NAME, rawToken)
                .httpOnly(true)
                .secure(props.isCookieSecure())
                .sameSite(props.getCookieSameSite())
                .path(COOKIE_PATH)
                .maxAge(Duration.ofDays(props.getExpirationDays()))
                .build();
    }

    public ResponseCookie clearCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(props.isCookieSecure())
                .sameSite(props.getCookieSameSite())
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
