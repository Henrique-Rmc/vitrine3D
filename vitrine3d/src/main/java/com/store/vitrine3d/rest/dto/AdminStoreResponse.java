package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.Subscription;

import java.time.Instant;
import java.util.UUID;

public class AdminStoreResponse {

    private UUID id;
    private String email;
    private String userName;
    private String storeName;
    private String slug;
    private Boolean isActive;
    private boolean emailVerified;
    private String profileType;
    private Instant createdAt;
    private SubscriptionResponse subscription;

    public static AdminStoreResponse from(Store store, Subscription sub) {
        AdminStoreResponse dto = new AdminStoreResponse();
        dto.id = store.getId();
        dto.email = store.getEmail();
        dto.userName = store.getUserName();
        dto.storeName = store.getStoreName();
        dto.slug = store.getSlug();
        dto.isActive = store.getIsActive();
        dto.emailVerified = store.isEmailVerified();
        dto.profileType = store.getProfileType().name();
        dto.createdAt = store.getCreatedAt();
        dto.subscription = SubscriptionResponse.from(sub);
        return dto;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getUserName() { return userName; }
    public String getStoreName() { return storeName; }
    public String getSlug() { return slug; }
    public Boolean getIsActive() { return isActive; }
    public boolean isEmailVerified() { return emailVerified; }
    public String getProfileType() { return profileType; }
    public Instant getCreatedAt() { return createdAt; }
    public SubscriptionResponse getSubscription() { return subscription; }
}
