package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.service.AttributeTypeHandler;
import com.store.vitrine3d.domain.specification.ProductSpec;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NumberAttributeTypeHandler implements AttributeTypeHandler {

    @Override
    public AttributeType type() {
        return AttributeType.NUMBER;
    }

    @Override
    public Object normalize(AttributeDefinition definition, Object rawValue) {
        if (rawValue instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        if (rawValue instanceof String text) {
            return parse(definition.getKey(), text);
        }
        throw new BusinessRuleException("INVALID_ATTRIBUTE_VALUE",
                "Attribute '" + definition.getKey() + "' must be a number.");
    }

    @Override
    public Specification<Product> equalsFilter(String key, String rawValue) {
        return ProductSpec.attributeNumberBetween(key, parse(key, rawValue), parse(key, rawValue));
    }

    @Override
    public Specification<Product> betweenFilter(String key, String rawMin, String rawMax) {
        BigDecimal min = rawMin != null ? parse(key, rawMin) : null;
        BigDecimal max = rawMax != null ? parse(key, rawMax) : null;
        return ProductSpec.attributeNumberBetween(key, min, max);
    }

    private BigDecimal parse(String key, String raw) {
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException e) {
            throw new BusinessRuleException("INVALID_FILTER_VALUE",
                    "Filter value for attribute '" + key + "' must be numeric.");
        }
    }
}
