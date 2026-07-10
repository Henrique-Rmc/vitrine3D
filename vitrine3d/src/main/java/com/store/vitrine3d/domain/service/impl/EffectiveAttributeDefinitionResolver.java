package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.StoreAttributeOption;
import com.store.vitrine3d.domain.repository.StoreAttributeOptionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolve a lista de valores ENUM efetiva de um atributo para uma loja especifica.
 * Atributos custom (AttributeDefinition.store != null) ja sao exclusivos de uma loja, entao
 * usam AttributeDefinition.enumOptions direto. Atributos globais (store == null) sao
 * compartilhados entre lojas da mesma vertical, entao os valores cadastrados por cada loja
 * vivem em StoreAttributeOption, nunca em AttributeDefinition.enumOptions.
 */
@Component
public class EffectiveAttributeDefinitionResolver {

    private final StoreAttributeOptionRepository storeAttributeOptionRepository;

    public EffectiveAttributeDefinitionResolver(StoreAttributeOptionRepository storeAttributeOptionRepository) {
        this.storeAttributeOptionRepository = storeAttributeOptionRepository;
    }

    public List<String> resolveEnumOptions(Store store, AttributeDefinition definition) {
        if (definition.getType() != AttributeType.ENUM) {
            return List.of();
        }
        if (definition.getStore() != null) {
            return definition.getEnumOptions();
        }
        return storeAttributeOptionRepository
                .findByStoreIdAndAttributeDefinitionIdOrderBySortOrderAsc(store.getId(), definition.getId())
                .stream()
                .map(StoreAttributeOption::getValue)
                .toList();
    }

    /**
     * Devolve uma copia nunca-persistida com enumOptions resolvido para a loja, sem jamais
     * chamar um setter na entidade gerenciada pelo Hibernate — mutar a colecao gerenciada
     * seria detectado pelo dirty-checking e vazaria os valores da loja para o registro
     * global compartilhado no commit da transacao.
     */
    public AttributeDefinition withEffectiveOptions(Store store, AttributeDefinition definition) {
        if (definition.getType() != AttributeType.ENUM || definition.getStore() != null) {
            return definition;
        }
        AttributeDefinition copy = new AttributeDefinition();
        copy.setId(definition.getId());
        copy.setBusinessType(definition.getBusinessType());
        copy.setStore(definition.getStore());
        copy.setKey(definition.getKey());
        copy.setLabel(definition.getLabel());
        copy.setType(definition.getType());
        copy.setUnit(definition.getUnit());
        copy.setRequired(definition.getRequired());
        copy.setFilterable(definition.getFilterable());
        copy.setSortOrder(definition.getSortOrder());
        copy.setEnumOptions(resolveEnumOptions(store, definition));
        return copy;
    }
}
