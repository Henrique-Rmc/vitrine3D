package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.State;
import lombok.Data;

@Data
public class StateResponse {
    private Long id;
    private String name;
    private String abbreviation;

    public static StateResponse from(State state) {
        StateResponse dto = new StateResponse();
        dto.setId(state.getId());
        dto.setName(state.getName());
        dto.setAbbreviation(state.getAbbreviation());
        return dto;
    }
}
