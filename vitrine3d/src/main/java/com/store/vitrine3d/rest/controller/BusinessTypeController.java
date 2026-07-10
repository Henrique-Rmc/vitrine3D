package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.repository.BusinessTypeRepository;
import com.store.vitrine3d.rest.dto.AttributeDefinitionResponse;
import com.store.vitrine3d.rest.dto.BusinessTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@Tag(name = "Tipos de negócio", description = "Segmentos de loja e seus atributos dinâmicos de produto")
@RestController
@RequestMapping("/api/business-types")
public class BusinessTypeController {

    private final BusinessTypeRepository businessTypeRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;

    public BusinessTypeController(BusinessTypeRepository businessTypeRepository,
                                   AttributeDefinitionRepository attributeDefinitionRepository) {
        this.businessTypeRepository = businessTypeRepository;
        this.attributeDefinitionRepository = attributeDefinitionRepository;
    }

    @Operation(summary = "Lista todos os tipos de negócio disponíveis para o cadastro da loja")
    @GetMapping
    public List<BusinessTypeResponse> listAll() {
        return businessTypeRepository.findAll().stream()
                .map(BusinessTypeResponse::from)
                .sorted(Comparator.comparing(BusinessTypeResponse::getName))
                .toList();
    }

    @Operation(summary = "Lista os atributos dinâmicos definidos para um tipo de negócio (formulário de produto e filtros)")
    @GetMapping("/{id}/attributes")
    public List<AttributeDefinitionResponse> listAttributes(@PathVariable Long id) {
        return attributeDefinitionRepository.findByBusinessTypeIdOrderBySortOrderAsc(id).stream()
                .map(AttributeDefinitionResponse::from)
                .toList();
    }
}
