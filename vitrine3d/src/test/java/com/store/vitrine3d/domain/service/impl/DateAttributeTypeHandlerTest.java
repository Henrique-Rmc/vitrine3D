package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateAttributeTypeHandlerTest {

    private final DateAttributeTypeHandler handler = new DateAttributeTypeHandler();

    private AttributeDefinition definition() {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setKey("validade");
        definition.setType(AttributeType.DATE);
        return definition;
    }

    @Test
    void normalize_acceptsIsoDate() {
        assertThat(handler.normalize(definition(), "2026-12-31")).isEqualTo("2026-12-31");
    }

    @Test
    void normalize_rejectsInvalidFormat() {
        assertThatThrownBy(() -> handler.normalize(definition(), "31/12/2026"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void equalsFilter_rejectsInvalidFormat() {
        assertThatThrownBy(() -> handler.equalsFilter("validade", "31/12/2026"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void equalsFilter_buildsSpecificationForValidDate() {
        assertThat(handler.equalsFilter("validade", "2026-12-31")).isNotNull();
    }
}
