package com.store.vitrine3d.rest.dto;

public class PlatformStatsResponse {

    private long totalStores;
    private long activeStores;
    private long totalProducts;
    private long totalWhatsappClicks;
    private long totalAffiliateClicks;

    public PlatformStatsResponse(long totalStores, long activeStores, long totalProducts,
                                  long totalWhatsappClicks, long totalAffiliateClicks) {
        this.totalStores = totalStores;
        this.activeStores = activeStores;
        this.totalProducts = totalProducts;
        this.totalWhatsappClicks = totalWhatsappClicks;
        this.totalAffiliateClicks = totalAffiliateClicks;
    }

    public long getTotalStores() { return totalStores; }
    public long getActiveStores() { return activeStores; }
    public long getTotalProducts() { return totalProducts; }
    public long getTotalWhatsappClicks() { return totalWhatsappClicks; }
    public long getTotalAffiliateClicks() { return totalAffiliateClicks; }
}
