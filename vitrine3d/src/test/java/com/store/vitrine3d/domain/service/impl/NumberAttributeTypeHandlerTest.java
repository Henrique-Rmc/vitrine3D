package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumberAttributeTypeHandlerTest {

    private final NumberAttributeTypeHandler handler = new NumberAttributeTypeHandler();

    private AttributeDefinition definition() {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setKey("ano");
        definition.setType(AttributeType.NUMBER);
        return definition;
    }

    @Test
    void normalize_acceptsNumber() {
        assertThat((BigDecimal) handler.normalize(definition(), 2020)).isEqualByComparingTo(new BigDecimal("2020"));
    }

    @Test
    void normalize_acceptsNumericString() {
        assertThat((BigDecimal) handler.normalize(definition(), "2020")).isEqualByComparingTo(new BigDecimal("2020"));
    }

    @Test
    void normalize_rejectsNonNumericString() {
        assertThatThrownBy(() -> handler.normalize(definition(), "abc"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void equalsFilter_rejectsNonNumericFilterValue() {
        assertThatThrownBy(() -> handler.equalsFilter("ano", "abc"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void equalsFilter_buildsSpecificationForValidValue() {
        assertThat(handler.equalsFilter("ano", "2020")).isNotNull();
    }

    @Test
    void betweenFilter_buildsSpecificationWithOnlyMin() {
        assertThat(handler.betweenFilter("ano", "2015", null)).isNotNull();
    }
}
