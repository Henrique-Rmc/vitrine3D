package com.store.vitrine3d.infrastructure.security;

import com.store.vitrine3d.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String ISSUER = "vitrine3d";
    private static final int MIN_SECRET_LENGTH = 32;

    private final JwtProperties props;
    private SecretKey signingKey;

    public JwtTokenProvider(JwtProperties props) {
        this.props = props;
    }

    @PostConstruct
    void init() {
        String secret = props.getSecret();
        Assert.notNull(secret, "jwt.secret não pode ser nulo — defina a variável JWT_SECRET no ambiente");
        Assert.isTrue(secret.length() >= MIN_SECRET_LENGTH,
                "jwt.secret deve ter no mínimo " + MIN_SECRET_LENGTH + " caracteres — defina a variável JWT_SECRET no ambiente");
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JwtTokenProvider inicializado — expiração configurada para {}ms", props.getExpiration());
    }

    public String generateToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + props.getExpiration()))
                .signWith(signingKey)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("Token JWT vazio ou nulo");
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .requireIssuer(ISSUER)
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
