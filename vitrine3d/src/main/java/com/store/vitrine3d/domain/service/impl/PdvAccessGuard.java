package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Subscription;
import com.store.vitrine3d.domain.model.SubscriptionPlan;
import com.store.vitrine3d.domain.repository.SubscriptionRepository;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PdvAccessGuard {

    private final SubscriptionRepository subscriptionRepository;

    public PdvAccessGuard(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public void assertAccess(UUID storeId) {
        Subscription sub = subscriptionRepository.findByStoreId(storeId)
                .orElseThrow(() -> new BusinessRuleException("PDV_NOT_AVAILABLE", "PDV requer plano Premium"));
        if (sub.getPlan() != SubscriptionPlan.PREMIUM) {
            throw new BusinessRuleException("PDV_NOT_AVAILABLE",
                    "PDV requer plano Premium. Plano atual: " + sub.getPlan());
        }
    }
}
