package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.repository.StoreTemplateRepository;
import com.store.vitrine3d.rest.dto.StoreTemplateResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@Tag(name = "Store Templates", description = "Templates de segmento — retorna os presets disponíveis no cadastro")
@RestController
@RequestMapping("/api/store-templates")
public class StoreTemplateController {

    private final StoreTemplateRepository repository;

    public StoreTemplateController(StoreTemplateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<StoreTemplateResponse> listAll() {
        return repository.findAll().stream()
                .map(StoreTemplateResponse::from)
                .sorted(Comparator.comparing(StoreTemplateResponse::getName))
                .toList();
    }
}
