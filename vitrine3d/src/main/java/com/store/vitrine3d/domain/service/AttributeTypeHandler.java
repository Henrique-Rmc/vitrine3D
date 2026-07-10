package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.springframework.data.jpa.domain.Specification;

public interface AttributeTypeHandler {

    AttributeType type();

    /**
     * Valida o valor bruto recebido na requisicao contra a definicao do atributo
     * e devolve a representacao normalizada a ser persistida em Product.attributes.
     */
    Object normalize(AttributeDefinition definition, Object rawValue);

    /** Predicado JSONB de igualdade (Postgres-only — ver ProductSpec). */
    Specification<Product> equalsFilter(String key, String rawValue);

    /** Predicado JSONB de faixa (min/max). Só sobrescrito pelos tipos que suportam faixa. */
    default Specification<Product> betweenFilter(String key, String rawMin, String rawMax) {
        throw new BusinessRuleException("ATTRIBUTE_RANGE_UNSUPPORTED",
                "Range filter is not supported for attribute type " + type());
    }
}
