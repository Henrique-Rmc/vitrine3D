package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.model.Store;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

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
    public ResponseEntity<StoreResponse> update(@PathVariable UUID id,
                                                @Valid @RequestBody StoreUpdateRequest request,
                                                @AuthenticationPrincipal UserDetails principal) {
        if (!isOwner(principal, id)) {
            throw new AccessDeniedException("You do not have permission to modify this store.");
        }
        return ResponseEntity.ok(StoreResponse.from(userService.update(id, request)));
    }

    @Operation(summary = "Faz upload do logo da loja")
    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreResponse> uploadLogo(@PathVariable UUID id,
                                                    @RequestPart("logo") MultipartFile logo,
                                                    @AuthenticationPrincipal UserDetails principal) {
        if (!isOwner(principal, id)) {
            throw new AccessDeniedException("You do not have permission to modify this store.");
        }
        return ResponseEntity.ok(StoreResponse.from(userService.uploadLogo(id, logo)));
    }

    @Operation(summary = "Busca lojista por ID — perfil público")
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getById(@PathVariable UUID id) {
        return userService.findById(id)
                .map(store -> ResponseEntity.ok(StoreResponse.fromPublic(store)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Perfil público da loja por slug")
    @GetMapping("/store/{slug}")
    public ResponseEntity<StoreResponse> getBySlug(@PathVariable String slug) {
        return userService.findBySlug(slug)
                .map(store -> ResponseEntity.ok(StoreResponse.fromPublic(store)))
                .orElse(ResponseEntity.notFound().build());
    }

    private boolean isOwner(UserDetails principal, UUID targetId) {
        return userService.findByEmail(principal.getUsername())
                .map(Store::getId)
                .map(ownerId -> ownerId.equals(targetId))
                .orElse(false);
    }
}
