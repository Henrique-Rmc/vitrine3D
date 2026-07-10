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
public class BooleanAttributeTypeHandler implements AttributeTypeHandler {

    @Override
    public AttributeType type() {
        return AttributeType.BOOLEAN;
    }

    @Override
    public Object normalize(AttributeDefinition definition, Object rawValue) {
        if (rawValue instanceof Boolean bool) {
            return bool;
        }
        if (rawValue instanceof String text && (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false"))) {
            return Boolean.parseBoolean(text);
        }
        throw new BusinessRuleException("INVALID_ATTRIBUTE_VALUE",
                "Attribute '" + definition.getKey() + "' must be true or false.");
    }

    @Override
    public Specification<Product> equalsFilter(String key, String rawValue) {
        if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false")) {
            throw new BusinessRuleException("INVALID_FILTER_VALUE",
                    "Filter value for attribute '" + key + "' must be true or false.");
        }
        return ProductSpec.attributeEquals(key, String.valueOf(Boolean.parseBoolean(rawValue)));
    }
}
