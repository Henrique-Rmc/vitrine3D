package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.Store;
import lombok.Data;

import java.util.UUID;

@Data
public class StoreResponse {

    private UUID id;
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

    // Resposta completa — para o próprio lojista autenticado
    public static StoreResponse from(Store store) {
        StoreResponse dto = baseFields(store);
        dto.setEmail(store.getEmail());
        dto.setUserName(store.getUserName());
        dto.setIsActive(store.getIsActive());
        return dto;
    }

    // Resposta pública — sem dados pessoais (email, userName, isActive)
    public static StoreResponse fromPublic(Store store) {
        return baseFields(store);
    }

    private static StoreResponse baseFields(Store store) {
        StoreResponse dto = new StoreResponse();
        dto.setId(store.getId());
        dto.setStoreName(store.getStoreName());
        dto.setSlug(store.getSlug());
        dto.setWhatsappNumber(store.getWhatsappNumber());
        dto.setStoreDescription(store.getStoreDescription());
        dto.setLogoUrl(store.getLogoUrl());

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
