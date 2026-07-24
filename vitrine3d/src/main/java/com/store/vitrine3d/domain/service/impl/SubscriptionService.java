package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.Subscription;
import com.store.vitrine3d.domain.model.SubscriptionPlan;
import com.store.vitrine3d.domain.model.SubscriptionStatus;
import com.store.vitrine3d.domain.repository.SubscriptionRepository;
import com.store.vitrine3d.rest.dto.SubscriptionAdminUpdateRequest;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription createTrialFor(Store store) {
        Subscription sub = new Subscription();
        sub.setStore(store);
        sub.setPlan(SubscriptionPlan.FREE);
        sub.setStatus(SubscriptionStatus.TRIAL);
        sub.setStartedAt(Instant.now());
        sub.setTrialEndsAt(Instant.now().plus(30, ChronoUnit.DAYS));
        return subscriptionRepository.save(sub);
    }

    @Transactional(readOnly = true)
    public Optional<Subscription> findByStoreId(UUID storeId) {
        return subscriptionRepository.findByStoreId(storeId);
    }

    @Transactional(readOnly = true)
    public Subscription getByStoreId(UUID storeId) {
        return subscriptionRepository.findByStoreId(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", storeId.toString()));
    }

    @Transactional(readOnly = true)
    public boolean isActive(UUID storeId) {
        return subscriptionRepository.findByStoreId(storeId)
                .map(sub -> {
                    if (sub.getStatus() == SubscriptionStatus.TRIAL) {
                        return sub.getTrialEndsAt() != null && Instant.now().isBefore(sub.getTrialEndsAt());
                    }
                    return sub.getStatus() == SubscriptionStatus.ACTIVE;
                })
                .orElse(false);
    }

    public Subscription updateForAdmin(UUID storeId, SubscriptionAdminUpdateRequest req) {
        Subscription sub = getByStoreId(storeId);
        if (req.getPlan() != null) sub.setPlan(req.getPlan());
        if (req.getStatus() != null) sub.setStatus(req.getStatus());
        if (req.getTrialEndsAt() != null) sub.setTrialEndsAt(req.getTrialEndsAt());
        if (req.getExpiresAt() != null) sub.setExpiresAt(req.getExpiresAt());
        if (req.getCancelledAt() != null) sub.setCancelledAt(req.getCancelledAt());
        if (req.getPaymentProvider() != null) sub.setPaymentProvider(req.getPaymentProvider());
        if (req.getExternalId() != null) sub.setExternalId(req.getExternalId());
        return subscriptionRepository.save(sub);
    }

    public Subscription extendTrial(UUID storeId, int days) {
        Subscription sub = getByStoreId(storeId);
        Instant base = (sub.getTrialEndsAt() != null && Instant.now().isBefore(sub.getTrialEndsAt()))
                ? sub.getTrialEndsAt()
                : Instant.now();
        sub.setTrialEndsAt(base.plus(days, ChronoUnit.DAYS));
        if (sub.getStatus() != SubscriptionStatus.TRIAL) {
            sub.setStatus(SubscriptionStatus.TRIAL);
        }
        return subscriptionRepository.save(sub);
    }
}
