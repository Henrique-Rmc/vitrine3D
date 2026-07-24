package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.Subscription;
import com.store.vitrine3d.domain.repository.AffiliateClickRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.repository.SubscriptionRepository;
import com.store.vitrine3d.domain.repository.WhatsappClickRepository;
import com.store.vitrine3d.rest.dto.AdminStoreFilter;
import com.store.vitrine3d.rest.dto.AdminStoreResponse;
import com.store.vitrine3d.rest.dto.PlatformStatsResponse;
import com.store.vitrine3d.rest.dto.SubscriptionAdminUpdateRequest;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AdminService {

    private final StoreRepository storeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final ProductRepository productRepository;
    private final WhatsappClickRepository whatsappClickRepository;
    private final AffiliateClickRepository affiliateClickRepository;

    public AdminService(StoreRepository storeRepository,
                        SubscriptionRepository subscriptionRepository,
                        SubscriptionService subscriptionService,
                        ProductRepository productRepository,
                        WhatsappClickRepository whatsappClickRepository,
                        AffiliateClickRepository affiliateClickRepository) {
        this.storeRepository = storeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
        this.productRepository = productRepository;
        this.whatsappClickRepository = whatsappClickRepository;
        this.affiliateClickRepository = affiliateClickRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminStoreResponse> listStores(AdminStoreFilter filter, int page, int size) {
        Specification<Store> spec = buildSpec(filter);
        Page<Store> stores = storeRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return stores.map(s -> AdminStoreResponse.from(s,
                subscriptionRepository.findByStoreId(s.getId()).orElse(null)));
    }

    @Transactional(readOnly = true)
    public AdminStoreResponse getStore(UUID storeId) {
        Store store = requireStore(storeId);
        Subscription sub = subscriptionRepository.findByStoreId(storeId).orElse(null);
        return AdminStoreResponse.from(store, sub);
    }

    public AdminStoreResponse toggleActive(UUID storeId) {
        Store store = requireStore(storeId);
        store.setIsActive(!Boolean.TRUE.equals(store.getIsActive()));
        storeRepository.save(store);
        Subscription sub = subscriptionRepository.findByStoreId(storeId).orElse(null);
        return AdminStoreResponse.from(store, sub);
    }

    public void deleteStore(UUID storeId) {
        Store store = requireStore(storeId);
        subscriptionRepository.findByStoreId(storeId).ifPresent(subscriptionRepository::delete);
        storeRepository.delete(store);
    }

    public AdminStoreResponse updateSubscription(UUID storeId, SubscriptionAdminUpdateRequest req) {
        Subscription sub = subscriptionService.updateForAdmin(storeId, req);
        return AdminStoreResponse.from(sub.getStore(), sub);
    }

    public AdminStoreResponse extendTrial(UUID storeId, int days) {
        Subscription sub = subscriptionService.extendTrial(storeId, days);
        return AdminStoreResponse.from(sub.getStore(), sub);
    }

    @Transactional(readOnly = true)
    public PlatformStatsResponse getStats() {
        long totalStores = storeRepository.count();
        long activeStores = storeRepository.count(
                (root, query, cb) -> cb.isTrue(root.get("isActive")));
        long totalProducts = productRepository.count();
        long totalWhatsappClicks = whatsappClickRepository.count();
        long totalAffiliateClicks = affiliateClickRepository.count();
        return new PlatformStatsResponse(totalStores, activeStores, totalProducts,
                totalWhatsappClicks, totalAffiliateClicks);
    }

    private Store requireStore(UUID storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeId.toString()));
    }

    private Specification<Store> buildSpec(AdminStoreFilter filter) {
        Specification<Store> spec = Specification.allOf();

        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            String pattern = "%" + filter.getSearch().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("storeName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("slug")), pattern)
            ));
        }

        if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("email")), filter.getEmail().toLowerCase()));
        }

        if (filter.getStoreName() != null && !filter.getStoreName().isBlank()) {
            String pattern = "%" + filter.getStoreName().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("storeName")), pattern));
        }

        if (filter.getActive() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("isActive"), filter.getActive()));
        }

        return spec;
    }
}
