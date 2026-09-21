package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.FamilyRelationship;
import com.store.vitrine3d.domain.model.PdvCustomer;
import com.store.vitrine3d.domain.model.PdvCustomerRelationship;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PdvCustomerRelationshipResponse {

    private Long id;
    private UUID relativeId;
    private String relativeName;
    private String relativePhone;
    private FamilyRelationship relationship;
    // true = o cliente consultado é a origem (customer_id); false = é o destino (relative_id)
    private boolean fromMe;
    private String note;
    private Instant createdAt;

    public static PdvCustomerRelationshipResponse from(PdvCustomerRelationship rel, UUID viewingCustomerId) {
        PdvCustomerRelationshipResponse dto = new PdvCustomerRelationshipResponse();
        dto.setId(rel.getId());
        dto.setRelationship(rel.getRelationship());
        dto.setNote(rel.getNote());
        dto.setCreatedAt(rel.getCreatedAt());

        boolean fromMe = rel.getCustomer().getId().equals(viewingCustomerId);
        dto.setFromMe(fromMe);

        PdvCustomer other = fromMe ? rel.getRelative() : rel.getCustomer();
        dto.setRelativeId(other.getId());
        dto.setRelativeName(other.getName());
        dto.setRelativePhone(other.getPhone());

        return dto;
    }
}
