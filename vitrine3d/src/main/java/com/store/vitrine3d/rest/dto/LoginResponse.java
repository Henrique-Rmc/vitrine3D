package com.store.vitrine3d.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String type = "Bearer";
    private UUID storeId;
    private String email;
    private String storeName;

    public LoginResponse(String accessToken, UUID storeId, String email, String storeName) {
        this.accessToken = accessToken;
        this.storeId = storeId;
        this.email = email;
        this.storeName = storeName;
    }
}
