package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.PdvEmployee;
import com.store.vitrine3d.domain.model.PdvEmployeeRole;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PdvEmployeeResponse {
    private UUID id;
    private String name;
    private PdvEmployeeRole role;
    private Boolean isActive;
    private Instant createdAt;

    public static PdvEmployeeResponse from(PdvEmployee e) {
        PdvEmployeeResponse dto = new PdvEmployeeResponse();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setRole(e.getRole());
        dto.setIsActive(e.getIsActive());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
