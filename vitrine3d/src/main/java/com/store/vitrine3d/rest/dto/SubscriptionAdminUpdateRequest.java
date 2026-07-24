package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.PaymentProvider;
import com.store.vitrine3d.domain.model.SubscriptionPlan;
import com.store.vitrine3d.domain.model.SubscriptionStatus;

import java.time.Instant;

public class SubscriptionAdminUpdateRequest {

    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    private Instant trialEndsAt;
    private Instant expiresAt;
    private Instant cancelledAt;
    private PaymentProvider paymentProvider;
    private String externalId;

    public SubscriptionPlan getPlan() { return plan; }
    public void setPlan(SubscriptionPlan plan) { this.plan = plan; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public Instant getTrialEndsAt() { return trialEndsAt; }
    public void setTrialEndsAt(Instant trialEndsAt) { this.trialEndsAt = trialEndsAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }

    public PaymentProvider getPaymentProvider() { return paymentProvider; }
    public void setPaymentProvider(PaymentProvider paymentProvider) { this.paymentProvider = paymentProvider; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
}
