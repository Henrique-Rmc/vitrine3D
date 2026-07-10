package com.store.vitrine3d.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilter {
    private String keyword;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    /**
     * Filtros dinamicos por atributo do tipo de negocio da loja. Chave igual ao
     * AttributeDefinition.key para igualdade (ex.: "marca") ou com sufixo "_min"/"_max"
     * para faixa em atributos NUMBER/DATE (ex.: "ano_min", "ano_max").
     * Ligado via query string: attributes[marca]=Toyota&attributes[ano_min]=2020
     */
    private Map<String, String> attributes;
}
