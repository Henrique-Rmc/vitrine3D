package com.store.vitrine3d.rest.dto;

public class RefreshResponse {
    private final String accessToken;
    private final String type = "Bearer";

    public RefreshResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() { return accessToken; }
    public String getType() { return type; }
}
