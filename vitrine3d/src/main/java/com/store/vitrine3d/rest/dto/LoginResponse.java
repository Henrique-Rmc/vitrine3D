package com.store.vitrine3d.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String type = "Bearer";
    /** STORE_OWNER or ADMIN */
    private String role;
    private String email;
    /** Display name: storeName for stores, adminUser.name for admins */
    private String name;
    /** Null for admin users */
    private UUID storeId;

    public LoginResponse(String accessToken, String role, String email, String name, UUID storeId) {
        this.accessToken = accessToken;
        this.role = role;
        this.email = email;
        this.name = name;
        this.storeId = storeId;
    }
}
