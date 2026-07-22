package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.repository.BusinessTypeRepository;
import com.store.vitrine3d.rest.dto.BusinessTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/**
 * Tipos de negocio sao apenas um rotulo que a loja escolhe no cadastro, usado so pra
 * tracking/analytics ("qual segmento mais usa o site") — nao tem mais nenhum efeito no
 * schema de atributos (esse papel e do ProductType, que cada loja monta pra si mesma).
 */
@Tag(name = "Tipos de negócio", description = "Segmentos de loja, usados só para tracking/analytics no cadastro")
@RestController
@RequestMapping("/api/business-types")
public class BusinessTypeController {

    private final BusinessTypeRepository businessTypeRepository;

    public BusinessTypeController(BusinessTypeRepository businessTypeRepository) {
        this.businessTypeRepository = businessTypeRepository;
    }

    @Operation(summary = "Lista todos os tipos de negócio disponíveis para o cadastro da loja")
    @GetMapping
    public List<BusinessTypeResponse> listAll() {
        return businessTypeRepository.findAll().stream()
                .map(BusinessTypeResponse::from)
                .sorted(Comparator.comparing(BusinessTypeResponse::getName))
                .toList();
    }
}
