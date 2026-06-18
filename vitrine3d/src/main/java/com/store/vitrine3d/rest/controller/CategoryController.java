package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.model.Category;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.CategoryService;
import com.store.vitrine3d.rest.dto.CategoryCreateRequest;
import com.store.vitrine3d.rest.dto.CategoryResponse;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Categorias", description = "Gerenciamento de categorias globais e por loja")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final StoreRepository storeRepository;

    public CategoryController(CategoryService categoryService, StoreRepository storeRepository) {
        this.categoryService = categoryService;
        this.storeRepository = storeRepository;
    }

    @Operation(summary = "Lista apenas categorias globais (público)")
    @GetMapping
    public List<CategoryResponse> findPublic() {
        return categoryService.findPublic().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Operation(summary = "Lista categorias globais + categorias da loja (público)")
    @GetMapping("/store/{storeId}")
    public List<CategoryResponse> findByStore(@PathVariable UUID storeId) {
        return categoryService.findByStore(storeId).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Operation(summary = "Cria uma nova categoria para a loja autenticada")
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest request,
                                                   @AuthenticationPrincipal UserDetails principal) {
        Store store = storeRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Loja", principal.getUsername()));

        Category category = new Category();
        category.setName(request.getName());
        category.setIsGlobal(false);
        category.setStore(store);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryResponse.from(categoryService.save(category)));
    }

    @Operation(summary = "Atualiza nome de uma categoria da loja autenticada")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.ok(CategoryResponse.from(categoryService.update(id, request.getName())));
    }

    @Operation(summary = "Remove uma categoria da loja autenticada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
