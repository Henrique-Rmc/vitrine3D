package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.Store;
import lombok.Data;

@Data
public class StoreResponse {

    private Long id;
    private String email;
    private String userName;
    private String storeName;
    private String whatsappNumber;
    private String storeDescription;
    private String logoUrl;
    private Boolean isActive;
    private Long stateId;
    private String stateName;
    private String stateAbbreviation;
    private Long cityId;
    private String cityName;
    private String slug;

    public static StoreResponse from(Store store) {
        StoreResponse dto = new StoreResponse();
        dto.setId(store.getId());
        dto.setEmail(store.getEmail());
        dto.setUserName(store.getUserName());
        dto.setStoreName(store.getStoreName());
        dto.setSlug(store.getSlug());
        dto.setWhatsappNumber(store.getWhatsappNumber());
        dto.setStoreDescription(store.getStoreDescription());
        dto.setLogoUrl(store.getLogoUrl());
        dto.setIsActive(store.getIsActive());

        if (store.getState() != null) {
            dto.setStateId(store.getState().getId());
            dto.setStateName(store.getState().getName());
            dto.setStateAbbreviation(store.getState().getAbbreviation());
        }
        if (store.getCity() != null) {
            dto.setCityId(store.getCity().getId());
            dto.setCityName(store.getCity().getName());
        }

        return dto;
    }
}
