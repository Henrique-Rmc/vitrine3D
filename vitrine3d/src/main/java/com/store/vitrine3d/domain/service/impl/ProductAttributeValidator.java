package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.BusinessType;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.service.AttributeTypeHandler;
import com.store.vitrine3d.domain.service.AttributeTypeHandlerRegistry;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Valida os atributos dinamicos de um Product (Product.attributes) contra o schema
 * (AttributeDefinition) do tipo de negocio da loja dona do produto.
 */
@Component
public class ProductAttributeValidator {

    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeTypeHandlerRegistry handlerRegistry;
    private final EffectiveAttributeDefinitionResolver effectiveAttributeDefinitionResolver;

    public ProductAttributeValidator(AttributeDefinitionRepository attributeDefinitionRepository,
                                      AttributeTypeHandlerRegistry handlerRegistry,
                                      EffectiveAttributeDefinitionResolver effectiveAttributeDefinitionResolver) {
        this.attributeDefinitionRepository = attributeDefinitionRepository;
        this.handlerRegistry = handlerRegistry;
        this.effectiveAttributeDefinitionResolver = effectiveAttributeDefinitionResolver;
    }

    /** Usado na criacao do produto: exige todos os atributos obrigatorios do tipo de negocio. */
    public Map<String, Object> validateForCreate(Store store, Map<String, Object> rawAttributes) {
        Map<String, Object> raw = rawAttributes != null ? rawAttributes : Map.of();
        Map<String, AttributeDefinition> definitionsByKey = resolveDefinitions(store, raw);

        Map<String, Object> normalized = new HashMap<>();
        for (AttributeDefinition definition : definitionsByKey.values()) {
            Object value = raw.get(definition.getKey());
            if (value == null) {
                boolean hidden = store.getHiddenAttributeDefinitionIds().contains(definition.getId());
                if (Boolean.TRUE.equals(definition.getRequired()) && !hidden) {
                    throw new BusinessRuleException("MISSING_REQUIRED_ATTRIBUTE",
                            "Attribute '" + definition.getKey() + "' is required for this business type.");
                }
                continue;
            }
            normalized.put(definition.getKey(), normalize(definition, value));
        }
        return normalized;
    }

    /** Usado na atualizacao: so valida/normaliza as chaves enviadas e as sobrepoe nas existentes. */
    public Map<String, Object> validateForUpdate(Store store, Map<String, Object> existingAttributes,
                                                  Map<String, Object> incomingAttributes) {
        if (incomingAttributes == null || incomingAttributes.isEmpty()) {
            return existingAttributes;
        }
        Map<String, AttributeDefinition> definitionsByKey = resolveDefinitions(store, incomingAttributes);

        Map<String, Object> merged = new HashMap<>(existingAttributes != null ? existingAttributes : Map.of());
        for (Map.Entry<String, Object> entry : incomingAttributes.entrySet()) {
            AttributeDefinition definition = definitionsByKey.get(entry.getKey());
            merged.put(entry.getKey(), normalize(definition, entry.getValue()));
        }
        return merged;
    }

    private Map<String, AttributeDefinition> resolveDefinitions(Store store, Map<String, Object> rawAttributes) {
        BusinessType businessType = store.getBusinessType();
        if (businessType == null) {
            if (rawAttributes.isEmpty()) {
                return Map.of();
            }
            throw new BusinessRuleException("BUSINESS_TYPE_REQUIRED",
                    "Store must have a business type before setting product attributes.");
        }

        Map<String, AttributeDefinition> definitionsByKey = attributeDefinitionRepository
                .findEffectiveForStore(businessType.getId(), store.getId()).stream()
                .collect(Collectors.toMap(AttributeDefinition::getKey,
                        definition -> effectiveAttributeDefinitionResolver.withEffectiveOptions(store, definition)));

        for (String key : rawAttributes.keySet()) {
            if (!definitionsByKey.containsKey(key)) {
                throw new BusinessRuleException("UNKNOWN_ATTRIBUTE",
                        "Attribute '" + key + "' is not defined for business type '" + businessType.getName() + "'.");
            }
        }
        return definitionsByKey;
    }

    private Object normalize(AttributeDefinition definition, Object value) {
        AttributeTypeHandler handler = handlerRegistry.resolve(definition.getType());
        return handler.normalize(definition, value);
    }
}
