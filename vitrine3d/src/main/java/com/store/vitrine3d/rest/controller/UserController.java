package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import com.store.vitrine3d.domain.service.RefreshTokenService;
import com.store.vitrine3d.domain.service.UserService;
import com.store.vitrine3d.domain.service.impl.StoreAttributeDefinitionService;
import com.store.vitrine3d.domain.service.impl.SubscriptionService;
import com.store.vitrine3d.infrastructure.security.JwtTokenProvider;
import com.store.vitrine3d.rest.dto.AttributeDefinitionResponse;
import com.store.vitrine3d.rest.dto.LoginResponse;
import com.store.vitrine3d.rest.dto.StoreRegisterRequest;
import com.store.vitrine3d.rest.dto.StoreResponse;
import com.store.vitrine3d.rest.dto.StoreUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Usuários", description = "Cadastro e consulta de lojistas")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final StoreAttributeDefinitionService attributeService;
    private final CurrentStoreResolver currentStoreResolver;

    public UserController(UserService userService, SubscriptionService subscriptionService,
                          JwtTokenProvider jwtTokenProvider, RefreshTokenService refreshTokenService,
                          StoreAttributeDefinitionService attributeService,
                          CurrentStoreResolver currentStoreResolver) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.attributeService = attributeService;
        this.currentStoreResolver = currentStoreResolver;
    }

    @Operation(summary = "Cadastra um novo lojista — retorna access token para login imediato")
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody StoreRegisterRequest request) {
        return buildRegisterResponse(userService.register(request));
    }

    @Operation(summary = "Cadastra um lojista afiliado — profileType definido internamente como AFFILIATE")
    @PostMapping("/register/affiliate")
    public ResponseEntity<LoginResponse> registerAffiliate(@Valid @RequestBody StoreRegisterRequest request) {
        return buildRegisterResponse(userService.registerAffiliate(request));
    }

    private ResponseEntity<LoginResponse> buildRegisterResponse(Store store) {
        String accessToken = jwtTokenProvider.generateToken(store.getEmail());
        String rawRefreshToken = refreshTokenService.createFor(store);
        ResponseCookie cookie = refreshTokenService.buildCookie(rawRefreshToken);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(accessToken, "STORE_OWNER", store.getEmail(), store.getStoreName(), store.getId()));
    }

    @Operation(summary = "Lista atributos do lojista autenticado, filtrados por tipo de produto")
    @GetMapping("/me/attributes")
    public List<AttributeDefinitionResponse> listMyAttributes(
            @RequestParam(required = false) Long productTypeId) {
        Store store = currentStoreResolver.getCurrentStore();
        return attributeService.listEffective(store.getId(), productTypeId).stream()
                .map(AttributeDefinitionResponse::from)
                .toList();
    }

    @Operation(summary = "Atualiza dados do lojista")
    @PutMapping("/{id}")
    public ResponseEntity<StoreResponse> update(@PathVariable UUID id,
                                                @Valid @RequestBody StoreUpdateRequest request,
                                                @AuthenticationPrincipal UserDetails principal) {
        if (!isOwner(principal, id)) {
            throw new AccessDeniedException("You do not have permission to modify this store.");
        }
        Store store = userService.update(id, request);
        return ResponseEntity.ok(StoreResponse.from(store, subscriptionService.findByStoreId(id).orElse(null)));
    }

    @Operation(summary = "Faz upload do logo da loja")
    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreResponse> uploadLogo(@PathVariable UUID id,
                                                    @RequestPart("logo") MultipartFile logo,
                                                    @AuthenticationPrincipal UserDetails principal) {
        if (!isOwner(principal, id)) {
            throw new AccessDeniedException("You do not have permission to modify this store.");
        }
        Store store = userService.uploadLogo(id, logo);
        return ResponseEntity.ok(StoreResponse.from(store, subscriptionService.findByStoreId(id).orElse(null)));
    }

    @Operation(summary = "Faz upload da foto de capa da loja")
    @PostMapping(value = "/{id}/cover-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreResponse> uploadCoverImage(@PathVariable UUID id,
                                                          @RequestPart("coverImage") MultipartFile coverImage,
                                                          @AuthenticationPrincipal UserDetails principal) {
        if (!isOwner(principal, id)) {
            throw new AccessDeniedException("You do not have permission to modify this store.");
        }
        Store store = userService.uploadCoverImage(id, coverImage);
        return ResponseEntity.ok(StoreResponse.from(store, subscriptionService.findByStoreId(id).orElse(null)));
    }

    @Operation(summary = "Faz upload das fotos informativas da loja (até 3 — substitui todas as existentes)")
    @PostMapping(value = "/{id}/promo-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreResponse> uploadPromoImages(@PathVariable UUID id,
                                                           @RequestPart(value = "promoImages", required = false) List<MultipartFile> promoImages,
                                                           @AuthenticationPrincipal UserDetails principal) {
        if (!isOwner(principal, id)) {
            throw new AccessDeniedException("You do not have permission to modify this store.");
        }
        Store store = userService.uploadPromoImages(id, promoImages);
        return ResponseEntity.ok(StoreResponse.from(store, subscriptionService.findByStoreId(id).orElse(null)));
    }

    @Operation(summary = "Busca lojista por ID — perfil público")
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getById(@PathVariable UUID id) {
        return userService.findById(id)
                .map(store -> ResponseEntity.ok(StoreResponse.fromPublic(store)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Perfil público da loja por slug. Retorna 301 se o slug for histórico.")
    @GetMapping("/store/{slug}")
    public ResponseEntity<StoreResponse> getBySlug(@PathVariable String slug) {
        Store store = userService.findBySlug(slug).orElse(null);
        if (store == null) return ResponseEntity.notFound().build();
        if (!slug.equals(store.getSlug())) {
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .header("Location", "/api/users/store/" + store.getSlug())
                    .build();
        }
        return ResponseEntity.ok(StoreResponse.fromPublic(store));
    }

    private boolean isOwner(UserDetails principal, UUID targetId) {
        return userService.findByEmail(principal.getUsername())
                .map(Store::getId)
                .map(ownerId -> ownerId.equals(targetId))
                .orElse(false);
    }
}
