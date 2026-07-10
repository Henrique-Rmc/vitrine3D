package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BooleanAttributeTypeHandlerTest {

    private final BooleanAttributeTypeHandler handler = new BooleanAttributeTypeHandler();

    private AttributeDefinition definition() {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setKey("usado");
        definition.setType(AttributeType.BOOLEAN);
        return definition;
    }

    @Test
    void normalize_acceptsBooleanValue() {
        assertThat(handler.normalize(definition(), Boolean.TRUE)).isEqualTo(true);
    }

    @Test
    void normalize_acceptsStringTrueFalse() {
        assertThat(handler.normalize(definition(), "false")).isEqualTo(false);
    }

    @Test
    void normalize_rejectsInvalidString() {
        assertThatThrownBy(() -> handler.normalize(definition(), "maybe"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void equalsFilter_rejectsInvalidFilterValue() {
        assertThatThrownBy(() -> handler.equalsFilter("usado", "maybe"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void equalsFilter_buildsSpecificationForValidValue() {
        assertThat(handler.equalsFilter("usado", "true")).isNotNull();
    }
}
