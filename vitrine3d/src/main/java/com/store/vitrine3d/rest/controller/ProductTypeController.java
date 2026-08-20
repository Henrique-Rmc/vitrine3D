package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.service.impl.ProductTypeService;
import com.store.vitrine3d.rest.dto.ProductTypeCreateRequest;
import com.store.vitrine3d.rest.dto.ProductTypeResponse;
import com.store.vitrine3d.rest.dto.ProductTypeUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Tipos de produto", description = "Sub-categorias de produto criadas pela propria loja (ex.: Camisa, Action Figure)")
@RestController
@RequestMapping("/api/products/store/{storeId}/product-types")
public class ProductTypeController {

    private final ProductTypeService service;

    public ProductTypeController(ProductTypeService service) {
        this.service = service;
    }

    @Operation(summary = "Lista os tipos de produto cadastrados pela loja")
    @GetMapping
    public List<ProductTypeResponse> list(@PathVariable UUID storeId) {
        return service.listByStore(storeId).stream()
                .map(ProductTypeResponse::from)
                .toList();
    }

    @Operation(summary = "Cria um tipo de produto, exclusivo dessa loja")
    @PostMapping
    public ResponseEntity<ProductTypeResponse> create(
            @PathVariable UUID storeId,
            @Valid @RequestBody ProductTypeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductTypeResponse.from(service.create(storeId, request)));
    }

    @Operation(summary = "Renomeia o rotulo de exibicao de um tipo de produto")
    @PutMapping("/{productTypeId}")
    public ResponseEntity<ProductTypeResponse> update(
            @PathVariable UUID storeId,
            @PathVariable Long productTypeId,
            @Valid @RequestBody ProductTypeUpdateRequest request) {
        return ResponseEntity.ok(ProductTypeResponse.from(service.update(storeId, productTypeId, request)));
    }

    @Operation(summary = "Remove um tipo de produto (bloqueado se algum produto ainda o usa)")
    @DeleteMapping("/{productTypeId}")
    public ResponseEntity<Void> delete(@PathVariable UUID storeId, @PathVariable Long productTypeId) {
        service.delete(storeId, productTypeId);
        return ResponseEntity.noContent().build();
    }
}
