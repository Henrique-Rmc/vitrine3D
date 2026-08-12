package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.Subscription;
import com.store.vitrine3d.domain.repository.AffiliateClickRepository;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.repository.CartRepository;
import com.store.vitrine3d.domain.repository.OrderRepository;
import com.store.vitrine3d.domain.repository.PdvCashFlowRepository;
import com.store.vitrine3d.domain.repository.PdvCustomerCreditRepository;
import com.store.vitrine3d.domain.repository.PdvCustomerRepository;
import com.store.vitrine3d.domain.repository.PdvEmployeeRepository;
import com.store.vitrine3d.domain.repository.PdvSaleRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.ProductTypeRepository;
import com.store.vitrine3d.domain.repository.RefreshTokenRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.repository.StoreSlugHistoryRepository;
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
    private final ProductTypeRepository productTypeRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final StoreSlugHistoryRepository storeSlugHistoryRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final WhatsappClickRepository whatsappClickRepository;
    private final AffiliateClickRepository affiliateClickRepository;
    private final PdvSaleRepository pdvSaleRepository;
    private final PdvCashFlowRepository pdvCashFlowRepository;
    private final PdvCustomerCreditRepository pdvCustomerCreditRepository;
    private final PdvCustomerRepository pdvCustomerRepository;
    private final PdvEmployeeRepository pdvEmployeeRepository;

    public AdminService(StoreRepository storeRepository,
                        SubscriptionRepository subscriptionRepository,
                        SubscriptionService subscriptionService,
                        ProductRepository productRepository,
                        ProductTypeRepository productTypeRepository,
                        AttributeDefinitionRepository attributeDefinitionRepository,
                        CartRepository cartRepository,
                        OrderRepository orderRepository,
                        StoreSlugHistoryRepository storeSlugHistoryRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        WhatsappClickRepository whatsappClickRepository,
                        AffiliateClickRepository affiliateClickRepository,
                        PdvSaleRepository pdvSaleRepository,
                        PdvCashFlowRepository pdvCashFlowRepository,
                        PdvCustomerCreditRepository pdvCustomerCreditRepository,
                        PdvCustomerRepository pdvCustomerRepository,
                        PdvEmployeeRepository pdvEmployeeRepository) {
        this.storeRepository = storeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
        this.productRepository = productRepository;
        this.productTypeRepository = productTypeRepository;
        this.attributeDefinitionRepository = attributeDefinitionRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.storeSlugHistoryRepository = storeSlugHistoryRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.whatsappClickRepository = whatsappClickRepository;
        this.affiliateClickRepository = affiliateClickRepository;
        this.pdvSaleRepository = pdvSaleRepository;
        this.pdvCashFlowRepository = pdvCashFlowRepository;
        this.pdvCustomerCreditRepository = pdvCustomerCreditRepository;
        this.pdvCustomerRepository = pdvCustomerRepository;
        this.pdvEmployeeRepository = pdvEmployeeRepository;
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

    /**
     * Cascade policy for store deletion.
     *
     * HARD DELETE — data is purely derived, no independent value:
     *   whatsapp_clicks, affiliate_clicks   (leaf analytics; reference products)
     *   product_images                      (ElementCollection of products)
     *   attribute_definition_options        (ElementCollection of attribute_definitions)
     *   cart_items → carts                  (ephemeral shopping state, no fiscal value)
     *   products, attribute_definitions     (reference product_types)
     *   product_types, store_slug_history   (reference stores)
     *   subscriptions, refresh_tokens       (reference stores)
     *
     * PRESERVE — financial/transactional records:
     *   orders + order_items                orders.store_id is set to NULL;
     *                                       rows remain intact for accounting
     *                                       and dispute resolution.
     *
     * FK dependency order (children must be deleted before parents):
     *   whatsapp_clicks, affiliate_clicks → products
     *   product_images (EC), attribute_definition_options (EC)
     *   cart_items → carts → stores
     *   products, attribute_definitions → product_types → stores
     *   orders: store_id nulled (not deleted) — order_items stay linked to orders
     *   store_slug_history, subscriptions, refresh_tokens → stores
     */
    public void deleteStore(UUID storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId.toString());
        }

        // 1. Click analytics (leaf nodes referencing products)
        whatsappClickRepository.deleteAllByProductStoreId(storeId);
        affiliateClickRepository.deleteAllByProductStoreId(storeId);

        // 2. ElementCollection join tables (invisible to JPQL bulk delete)
        productRepository.deleteImagesByStoreId(storeId);
        attributeDefinitionRepository.deleteOptionsByStoreId(storeId);

        // 3. Cart line items, then carts (ephemeral — hard delete)
        cartRepository.deleteItemsByStoreId(storeId);
        cartRepository.deleteAllByStoreId(storeId);

        // 4. Orders: detach only — preserve as financial records
        orderRepository.detachFromStore(storeId);

        // 5. PDV data: sale items → sales → cash flows (operator FK resolved before employees)
        //              credit payments → credits → customers → employees
        pdvSaleRepository.deleteItemsByStoreId(storeId);
        pdvSaleRepository.deleteAllByStoreId(storeId);
        pdvCashFlowRepository.deleteAllByStoreId(storeId);
        pdvCustomerCreditRepository.deletePaymentsByStoreId(storeId);
        pdvCustomerCreditRepository.deleteAllByCustomerStoreId(storeId);
        pdvCustomerRepository.deleteAllByStoreId(storeId);
        pdvEmployeeRepository.deleteAllByStoreId(storeId);

        // 6. Products and attribute definitions (reference product_types)
        productRepository.deleteAllByStoreId(storeId);
        attributeDefinitionRepository.deleteAllByStoreId(storeId);

        // 6. Product types (referenced by products/attrs above — must come after)
        productTypeRepository.deleteAllByStoreId(storeId);

        // 7. Remaining store-level entities
        storeSlugHistoryRepository.deleteAllByStoreId(storeId);
        subscriptionRepository.deleteByStoreId(storeId);
        refreshTokenRepository.deleteAllByStoreId(storeId);

        // 8. The store itself
        storeRepository.deleteById(storeId);
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
