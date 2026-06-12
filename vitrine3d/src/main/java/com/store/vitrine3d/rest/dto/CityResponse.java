package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.City;
import lombok.Data;

@Data
public class CityResponse {
    private Long id;
    private String name;
    private Long stateId;
    private String stateAbbreviation;

    public static CityResponse from(City city) {
        CityResponse dto = new CityResponse();
        dto.setId(city.getId());
        dto.setName(city.getName());
        dto.setStateId(city.getState().getId());
        dto.setStateAbbreviation(city.getState().getAbbreviation());
        return dto;
    }
}
