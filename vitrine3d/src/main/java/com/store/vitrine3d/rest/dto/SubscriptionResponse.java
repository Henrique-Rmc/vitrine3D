package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.Subscription;
import com.store.vitrine3d.domain.model.SubscriptionPlan;
import com.store.vitrine3d.domain.model.SubscriptionStatus;

import java.time.Instant;

public class SubscriptionResponse {

    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    private Instant startedAt;
    private Instant trialEndsAt;
    private Instant expiresAt;
    private boolean active;

    public static SubscriptionResponse from(Subscription sub) {
        if (sub == null) return null;
        SubscriptionResponse dto = new SubscriptionResponse();
        dto.plan = sub.getPlan();
        dto.status = sub.getStatus();
        dto.startedAt = sub.getStartedAt();
        dto.trialEndsAt = sub.getTrialEndsAt();
        dto.expiresAt = sub.getExpiresAt();
        dto.active = isActive(sub);
        return dto;
    }

    private static boolean isActive(Subscription sub) {
        if (sub.getStatus() == SubscriptionStatus.TRIAL) {
            return sub.getTrialEndsAt() != null && Instant.now().isBefore(sub.getTrialEndsAt());
        }
        return sub.getStatus() == SubscriptionStatus.ACTIVE;
    }

    public SubscriptionPlan getPlan() { return plan; }
    public SubscriptionStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getTrialEndsAt() { return trialEndsAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }
}
