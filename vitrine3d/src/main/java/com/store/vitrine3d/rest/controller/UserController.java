package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.service.UserService;
import com.store.vitrine3d.rest.dto.StoreRegisterRequest;
import com.store.vitrine3d.rest.dto.StoreResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Usuários", description = "Cadastro e consulta de lojistas")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Cadastra um novo lojista")
    @PostMapping("/register")
    public ResponseEntity<StoreResponse> register(@Valid @RequestBody StoreRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StoreResponse.from(userService.register(request)));
    }

    @Operation(summary = "Busca lojista por ID")
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(store -> ResponseEntity.ok(StoreResponse.from(store)))
                .orElse(ResponseEntity.notFound().build());
    }
}
