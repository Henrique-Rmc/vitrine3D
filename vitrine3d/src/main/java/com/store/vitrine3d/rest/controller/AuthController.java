package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.service.UserService;
import com.store.vitrine3d.infrastructure.security.JwtTokenProvider;
import com.store.vitrine3d.rest.dto.LoginRequest;
import com.store.vitrine3d.rest.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação", description = "Login e geração de token JWT")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Operation(summary = "Autentica o lojista e retorna um token JWT")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtTokenProvider.generateToken(auth.getName());

        Store store = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Loja não encontrada após autenticação"));

        return ResponseEntity.ok(new LoginResponse(token, store.getId(), store.getEmail(), store.getStoreName()));
    }
}
