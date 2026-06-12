package com.store.vitrine3d.rest.dto;

import lombok.Data;

@Data
public class StoreUpdateRequest {
    private String userName;
    private String storeName;
    private String whatsappNumber;
    private String storeDescription;
    private Long stateId;
    private Long cityId;
}
