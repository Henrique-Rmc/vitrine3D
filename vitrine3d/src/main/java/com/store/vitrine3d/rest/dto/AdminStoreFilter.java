package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.SubscriptionPlan;
import com.store.vitrine3d.domain.model.SubscriptionStatus;

public class AdminStoreFilter {
    /** Full-text across storeName, email, slug */
    private String search;
    /** Exact email match */
    private String email;
    /** Partial storeName match */
    private String storeName;
    private Boolean active;
    private SubscriptionPlan plan;
    private SubscriptionStatus status;

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public SubscriptionPlan getPlan() { return plan; }
    public void setPlan(SubscriptionPlan plan) { this.plan = plan; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
}
