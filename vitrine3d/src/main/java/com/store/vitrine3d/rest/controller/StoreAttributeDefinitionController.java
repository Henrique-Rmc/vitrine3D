package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.service.impl.StoreAttributeDefinitionService;
import com.store.vitrine3d.rest.dto.AttributeDefinitionCreateRequest;
import com.store.vitrine3d.rest.dto.AttributeDefinitionResponse;
import com.store.vitrine3d.rest.dto.AttributeOptionCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Atributos da loja", description = "Atributos customizados por loja e opt-out de atributos globais")
@RestController
@RequestMapping("/api/products/store/{storeId}/attributes")
public class StoreAttributeDefinitionController {

    private final StoreAttributeDefinitionService service;

    public StoreAttributeDefinitionController(StoreAttributeDefinitionService service) {
        this.service = service;
    }

    @Operation(summary = "Lista os atributos efetivos da loja (globais nao escondidos + customizados dela)")
    @GetMapping
    public List<AttributeDefinitionResponse> listEffective(@PathVariable UUID storeId) {
        return service.listEffective(storeId).stream()
                .map(AttributeDefinitionResponse::from)
                .toList();
    }

    @Operation(summary = "Cria um atributo customizado, exclusivo dessa loja")
    @PostMapping
    public ResponseEntity<AttributeDefinitionResponse> createCustom(
            @PathVariable UUID storeId,
            @Valid @RequestBody AttributeDefinitionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AttributeDefinitionResponse.from(service.createCustom(storeId, request)));
    }

    @Operation(summary = "Remove um atributo customizado da loja (bloqueado se algum produto ainda o usa)")
    @DeleteMapping("/{attributeDefinitionId}")
    public ResponseEntity<Void> deleteCustom(@PathVariable UUID storeId, @PathVariable Long attributeDefinitionId) {
        service.deleteCustom(storeId, attributeDefinitionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Esconde um atributo global da vertical para essa loja (nao apaga o atributo)")
    @PatchMapping("/{attributeDefinitionId}/hide")
    public ResponseEntity<Void> hideGlobal(@PathVariable UUID storeId, @PathVariable Long attributeDefinitionId) {
        service.hideGlobal(storeId, attributeDefinitionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reexibe um atributo global previamente escondido")
    @PatchMapping("/{attributeDefinitionId}/unhide")
    public ResponseEntity<Void> unhideGlobal(@PathVariable UUID storeId, @PathVariable Long attributeDefinitionId) {
        service.unhideGlobal(storeId, attributeDefinitionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cadastra um valor de opcao pra um atributo do tipo lista (ENUM) — global da vertical ou customizado da propria loja")
    @PostMapping("/{attributeDefinitionId}/options")
    public ResponseEntity<AttributeDefinitionResponse> addOption(
            @PathVariable UUID storeId,
            @PathVariable Long attributeDefinitionId,
            @Valid @RequestBody AttributeOptionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AttributeDefinitionResponse.from(service.addOption(storeId, attributeDefinitionId, request.getValue())));
    }
}
