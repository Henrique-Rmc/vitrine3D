package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.model.AdminUser;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.AdminUserRepository;
import com.store.vitrine3d.domain.service.RefreshTokenService;
import com.store.vitrine3d.domain.service.UserService;
import com.store.vitrine3d.infrastructure.security.JwtTokenProvider;
import com.store.vitrine3d.rest.dto.LoginRequest;
import com.store.vitrine3d.rest.dto.LoginResponse;
import com.store.vitrine3d.rest.dto.RefreshResponse;
import com.store.vitrine3d.rest.exception.InvalidRefreshTokenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação", description = "Login, renovação e encerramento de sessão")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final long ADMIN_JWT_EXPIRATION_MS = 8 * 60 * 60 * 1000L;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AdminUserRepository adminUserRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          UserService userService,
                          RefreshTokenService refreshTokenService,
                          AdminUserRepository adminUserRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.adminUserRepository = adminUserRepository;
    }

    @Operation(summary = "Autentica usuário — retorna access token no body; refresh token em cookie httpOnly (apenas lojistas)")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String email = auth.getName();

        AdminUser admin = adminUserRepository.findByEmail(email).orElse(null);
        if (admin != null) {
            String accessToken = jwtTokenProvider.generateToken(email, ADMIN_JWT_EXPIRATION_MS);
            return ResponseEntity.ok()
                    .body(new LoginResponse(accessToken, "ADMIN", email, admin.getName(), null));
        }

        Store store = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Store not found after successful authentication"));

        String accessToken = jwtTokenProvider.generateToken(store.getEmail());
        String rawRefreshToken = refreshTokenService.createFor(store);
        ResponseCookie cookie = refreshTokenService.buildCookie(rawRefreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(accessToken, "STORE_OWNER", store.getEmail(), store.getStoreName(), store.getId()));
    }

    @Operation(summary = "Renova o access token usando o refresh token do cookie httpOnly")
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(HttpServletRequest request) {
        String rawToken = refreshTokenService.extractFromRequest(request);
        if (rawToken == null) {
            throw new InvalidRefreshTokenException("Refresh token cookie not found");
        }

        Store store = refreshTokenService.validateAndRevoke(rawToken);
        String newRawToken = refreshTokenService.createFor(store);
        String newAccessToken = jwtTokenProvider.generateToken(store.getEmail());
        ResponseCookie cookie = refreshTokenService.buildCookie(newRawToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new RefreshResponse(newAccessToken));
    }

    @Operation(summary = "Encerra a sessão — invalida o refresh token e limpa o cookie")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String rawToken = refreshTokenService.extractFromRequest(request);
        if (rawToken != null) {
            refreshTokenService.revokeByRawToken(rawToken);
        }
        ResponseCookie cleared = refreshTokenService.clearCookie();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cleared.toString())
                .build();
    }

    @Operation(summary = "Verifica o e-mail do lojista via token enviado no cadastro")
    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reenvia o link de verificação de e-mail (autenticado)")
    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@AuthenticationPrincipal UserDetails principal) {
        userService.resendVerification(principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
