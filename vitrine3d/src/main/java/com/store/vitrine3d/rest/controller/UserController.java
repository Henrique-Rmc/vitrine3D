package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.service.UserService;
import com.store.vitrine3d.rest.dto.StoreRegisterRequest;
import com.store.vitrine3d.rest.dto.StoreResponse;
import com.store.vitrine3d.rest.dto.StoreUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Operation(summary = "Atualiza dados do lojista")
    @PutMapping("/{id}")
    public ResponseEntity<StoreResponse> update(@PathVariable Long id,
                                                @RequestBody StoreUpdateRequest request) {
        return ResponseEntity.ok(StoreResponse.from(userService.update(id, request)));
    }

    @Operation(summary = "Faz upload do logo da loja")
    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreResponse> uploadLogo(@PathVariable Long id,
                                                    @RequestPart("logo") MultipartFile logo) {
        return ResponseEntity.ok(StoreResponse.from(userService.uploadLogo(id, logo)));
    }

    @Operation(summary = "Busca lojista por ID")
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(store -> ResponseEntity.ok(StoreResponse.from(store)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Perfil público da loja por slug")
    @GetMapping("/store/{slug}")
    public ResponseEntity<StoreResponse> getBySlug(@PathVariable String slug) {
        return userService.findBySlug(slug)
                .map(store -> ResponseEntity.ok(StoreResponse.from(store)))
                .orElse(ResponseEntity.notFound().build());
    }
}
