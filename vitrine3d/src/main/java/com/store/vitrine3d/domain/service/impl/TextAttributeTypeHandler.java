package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.service.AttributeTypeHandler;
import com.store.vitrine3d.domain.specification.ProductSpec;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TextAttributeTypeHandler implements AttributeTypeHandler {

    @Override
    public AttributeType type() {
        return AttributeType.TEXT;
    }

    @Override
    public Object normalize(AttributeDefinition definition, Object rawValue) {
        if (rawValue instanceof String text && !text.isBlank()) {
            return text;
        }
        throw new BusinessRuleException("INVALID_ATTRIBUTE_VALUE",
                "Attribute '" + definition.getKey() + "' must be a non-empty text.");
    }

    @Override
    public Specification<Product> equalsFilter(String key, String rawValue) {
        return ProductSpec.attributeEquals(key, rawValue);
    }
}
