package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.service.AttributeTypeHandler;
import com.store.vitrine3d.domain.specification.ProductSpec;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class DateAttributeTypeHandler implements AttributeTypeHandler {

    @Override
    public AttributeType type() {
        return AttributeType.DATE;
    }

    @Override
    public Object normalize(AttributeDefinition definition, Object rawValue) {
        if (rawValue instanceof String text) {
            return parse(definition.getKey(), text).toString();
        }
        throw new BusinessRuleException("INVALID_ATTRIBUTE_VALUE",
                "Attribute '" + definition.getKey() + "' must be a date in ISO format (yyyy-MM-dd).");
    }

    @Override
    public Specification<Product> equalsFilter(String key, String rawValue) {
        LocalDate date = parse(key, rawValue);
        return ProductSpec.attributeDateBetween(key, date, date);
    }

    @Override
    public Specification<Product> betweenFilter(String key, String rawMin, String rawMax) {
        LocalDate min = rawMin != null ? parse(key, rawMin) : null;
        LocalDate max = rawMax != null ? parse(key, rawMax) : null;
        return ProductSpec.attributeDateBetween(key, min, max);
    }

    private LocalDate parse(String key, String raw) {
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            throw new BusinessRuleException("INVALID_FILTER_VALUE",
                    "Value for attribute '" + key + "' must be a date in ISO format (yyyy-MM-dd).");
        }
    }
}
