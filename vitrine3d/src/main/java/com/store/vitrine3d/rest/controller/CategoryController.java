package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.model.Category;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.CategoryService;
import com.store.vitrine3d.rest.dto.CategoryCreateRequest;
import com.store.vitrine3d.rest.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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

    @Operation(summary = "Lista todas as categorias")
    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Operation(summary = "Cria uma nova categoria")
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setIsGlobal(Objects.requireNonNullElse(request.getIsGlobal(), Boolean.FALSE));

        if (request.getStoreId() != null) {
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new IllegalArgumentException("Loja não encontrada: " + request.getStoreId()));
            category.setStore(store);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryResponse.from(categoryService.save(category)));
    }

    @Operation(summary = "Atualiza nome de uma categoria")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.ok(CategoryResponse.from(categoryService.update(id, request.getName())));
    }

    @Operation(summary = "Remove uma categoria por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
