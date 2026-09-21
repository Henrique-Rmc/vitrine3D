package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.FamilyRelationship;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class PdvCustomerRelationshipRequest {

    @NotNull
    private UUID relativeId;

    @NotNull
    private FamilyRelationship relationship;

    @Size(max = 255)
    private String note;
}
