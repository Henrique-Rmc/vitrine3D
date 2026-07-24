package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;

// DORMANT — not wired into the system yet. Seeded via DataInitializer.
@Entity
@Table(name = "plan_limits")
public class PlanLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private SubscriptionPlan plan;

    // -1 means unlimited
    @Column(nullable = false)
    private int maxProducts;

    @Column(nullable = false)
    private int maxProductTypes;

    @Column(nullable = false)
    private int maxFeaturedProducts;

    @Column(nullable = false)
    private int maxPromoImages;

    @Column(nullable = false)
    private boolean affiliateProfileAllowed;

    @Column(nullable = false)
    private boolean customDomainAllowed;

    public PlanLimit() {}

    public PlanLimit(SubscriptionPlan plan, int maxProducts, int maxProductTypes,
                     int maxFeaturedProducts, int maxPromoImages,
                     boolean affiliateProfileAllowed, boolean customDomainAllowed) {
        this.plan = plan;
        this.maxProducts = maxProducts;
        this.maxProductTypes = maxProductTypes;
        this.maxFeaturedProducts = maxFeaturedProducts;
        this.maxPromoImages = maxPromoImages;
        this.affiliateProfileAllowed = affiliateProfileAllowed;
        this.customDomainAllowed = customDomainAllowed;
    }

    public Long getId() { return id; }

    public SubscriptionPlan getPlan() { return plan; }
    public void setPlan(SubscriptionPlan plan) { this.plan = plan; }

    public int getMaxProducts() { return maxProducts; }
    public void setMaxProducts(int maxProducts) { this.maxProducts = maxProducts; }

    public int getMaxProductTypes() { return maxProductTypes; }
    public void setMaxProductTypes(int maxProductTypes) { this.maxProductTypes = maxProductTypes; }

    public int getMaxFeaturedProducts() { return maxFeaturedProducts; }
    public void setMaxFeaturedProducts(int maxFeaturedProducts) { this.maxFeaturedProducts = maxFeaturedProducts; }

    public int getMaxPromoImages() { return maxPromoImages; }
    public void setMaxPromoImages(int maxPromoImages) { this.maxPromoImages = maxPromoImages; }

    public boolean isAffiliateProfileAllowed() { return affiliateProfileAllowed; }
    public void setAffiliateProfileAllowed(boolean affiliateProfileAllowed) { this.affiliateProfileAllowed = affiliateProfileAllowed; }

    public boolean isCustomDomainAllowed() { return customDomainAllowed; }
    public void setCustomDomainAllowed(boolean customDomainAllowed) { this.customDomainAllowed = customDomainAllowed; }
}
