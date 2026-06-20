package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.service.MaterialService;
import com.store.vitrine3d.rest.dto.MaterialCreateRequest;
import com.store.vitrine3d.rest.dto.MaterialResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Materiais", description = "Gerenciamento de materiais globais e por loja")
@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @Operation(summary = "Lista apenas materiais globais (público)")
    @GetMapping
    public List<MaterialResponse> findPublic() {
        return materialService.findPublic().stream().map(MaterialResponse::from).toList();
    }

    @Operation(summary = "Lista materiais globais + materiais da loja (público)")
    @GetMapping("/store/{storeId}")
    public List<MaterialResponse> findByStore(@PathVariable UUID storeId) {
        return materialService.findByStore(storeId).stream().map(MaterialResponse::from).toList();
    }

    @Operation(summary = "Cria um novo material para a loja autenticada")
    @PostMapping
    public ResponseEntity<MaterialResponse> create(@Valid @RequestBody MaterialCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MaterialResponse.from(materialService.create(request.getName())));
    }

    @Operation(summary = "Atualiza nome de um material da loja autenticada")
    @PutMapping("/{id}")
    public ResponseEntity<MaterialResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody MaterialCreateRequest request) {
        return ResponseEntity.ok(MaterialResponse.from(materialService.update(id, request.getName())));
    }

    @Operation(summary = "Remove um material da loja autenticada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        materialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
