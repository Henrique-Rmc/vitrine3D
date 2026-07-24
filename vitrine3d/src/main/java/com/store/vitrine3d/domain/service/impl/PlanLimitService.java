package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.PlanLimit;
import com.store.vitrine3d.domain.model.SubscriptionPlan;
import com.store.vitrine3d.domain.repository.PlanLimitRepository;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// DORMANT — not wired into the system yet.
// To activate: inject this service into ProductServiceImpl, UserServiceImpl, etc.
// and call the check methods before each create operation.
@Service
@Transactional(readOnly = true)
public class PlanLimitService {

    private final PlanLimitRepository planLimitRepository;
    private final SubscriptionService subscriptionService;

    public PlanLimitService(PlanLimitRepository planLimitRepository,
                            SubscriptionService subscriptionService) {
        this.planLimitRepository = planLimitRepository;
        this.subscriptionService = subscriptionService;
    }

    public PlanLimit getForPlan(SubscriptionPlan plan) {
        return planLimitRepository.findByPlan(plan)
                .orElseThrow(() -> new ResourceNotFoundException("PlanLimit", plan.name()));
    }

    // Returns the effective limit for a store based on its current subscription plan.
    // -1 means unlimited.
    public PlanLimit getForStore(UUID storeId) {
        SubscriptionPlan plan = subscriptionService.findByStoreId(storeId)
                .map(sub -> sub.getPlan())
                .orElse(SubscriptionPlan.FREE);
        return getForPlan(plan);
    }

    public boolean canAddProduct(UUID storeId, long currentProductCount) {
        PlanLimit limit = getForStore(storeId);
        return limit.getMaxProducts() == -1 || currentProductCount < limit.getMaxProducts();
    }

    public boolean canAddProductType(UUID storeId, long currentTypeCount) {
        PlanLimit limit = getForStore(storeId);
        return limit.getMaxProductTypes() == -1 || currentTypeCount < limit.getMaxProductTypes();
    }

    public boolean canFeatureProduct(UUID storeId, long currentFeaturedCount) {
        PlanLimit limit = getForStore(storeId);
        return limit.getMaxFeaturedProducts() == -1 || currentFeaturedCount < limit.getMaxFeaturedProducts();
    }

    public boolean canUseAffiliateProfile(UUID storeId) {
        return getForStore(storeId).isAffiliateProfileAllowed();
    }

    public boolean canUseCustomDomain(UUID storeId) {
        return getForStore(storeId).isCustomDomainAllowed();
    }
}
