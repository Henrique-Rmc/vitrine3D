package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import lombok.Data;

import java.util.List;

@Data
public class AttributeDefinitionResponse {
    private Long id;
    private String key;
    private String label;
    private AttributeType type;
    private String unit;
    private Boolean required;
    private Boolean filterable;
    private Integer sortOrder;
    private List<String> enumOptions;
    private Long productTypeId;

    public static AttributeDefinitionResponse from(AttributeDefinition definition) {
        AttributeDefinitionResponse dto = new AttributeDefinitionResponse();
        dto.setId(definition.getId());
        dto.setKey(definition.getKey());
        dto.setLabel(definition.getLabel());
        dto.setType(definition.getType());
        dto.setUnit(definition.getUnit());
        dto.setRequired(definition.getRequired());
        dto.setFilterable(definition.getFilterable());
        dto.setSortOrder(definition.getSortOrder());
        dto.setEnumOptions(definition.getEnumOptions());
        dto.setProductTypeId(definition.getProductType() != null ? definition.getProductType().getId() : null);
        return dto;
    }
}
