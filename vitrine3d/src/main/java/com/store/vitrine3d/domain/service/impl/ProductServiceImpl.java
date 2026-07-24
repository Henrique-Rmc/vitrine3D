package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AffiliateClick;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.model.ProductType;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.StoreProfileType;
import com.store.vitrine3d.domain.model.WhatsappClick;
import com.store.vitrine3d.domain.repository.AffiliateClickRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.ProductTypeRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.repository.WhatsappClickRepository;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import com.store.vitrine3d.domain.service.ProductService;
import com.store.vitrine3d.domain.specification.ProductSpec;
import com.store.vitrine3d.infrastructure.storage.StorageService;
import com.store.vitrine3d.rest.dto.ProductCreateRequest;
import com.store.vitrine3d.rest.dto.ProductFilter;
import com.store.vitrine3d.rest.dto.ProductUpdateRequest;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final int MAX_FEATURED = 5;
    private static final int MAX_IMAGES = 5;

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final ProductTypeRepository productTypeRepository;
    private final StorageService storageService;
    private final WhatsappClickRepository whatsappClickRepository;
    private final AffiliateClickRepository affiliateClickRepository;
    private final CurrentStoreResolver currentStoreResolver;
    private final ProductAttributeValidator attributeValidator;
    private final ProductAttributeFilterBuilder attributeFilterBuilder;

    public ProductServiceImpl(ProductRepository productRepository,
                               StoreRepository storeRepository,
                               ProductTypeRepository productTypeRepository,
                               StorageService storageService,
                               WhatsappClickRepository whatsappClickRepository,
                               AffiliateClickRepository affiliateClickRepository,
                               CurrentStoreResolver currentStoreResolver,
                               ProductAttributeValidator attributeValidator,
                               ProductAttributeFilterBuilder attributeFilterBuilder) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.productTypeRepository = productTypeRepository;
        this.storageService = storageService;
        this.whatsappClickRepository = whatsappClickRepository;
        this.affiliateClickRepository = affiliateClickRepository;
        this.currentStoreResolver = currentStoreResolver;
        this.attributeValidator = attributeValidator;
        this.attributeFilterBuilder = attributeFilterBuilder;
    }

    @Override
    public Product save(ProductCreateRequest request, List<MultipartFile> images) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store", request.getStoreId()));

        assertStoreOwnership(store);

        ProductType productType = resolveProductType(store, request.getProductTypeId());

        List<String> imageUrls = hasFiles(images) ? uploadImages(images) : nonNullList(request.getImageUrls());

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrls(imageUrls);
        product.setIsVisible(request.getIsVisible() != null ? request.getIsVisible() : Boolean.TRUE);
        product.setStore(store);
        product.setProductType(productType);
        product.setAffiliateUrl(resolveAffiliateUrl(store, request.getAffiliateUrl(), null));
        product.setAttributes(attributeValidator.validateForCreate(
                store, productType != null ? productType.getId() : null, request.getAttributes()));

        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, ProductUpdateRequest request, List<MultipartFile> images) {
        Product product = findById(id);
        assertProductOwnership(product);

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getIsVisible() != null) product.setIsVisible(request.getIsVisible());
        if (request.getFeatured() != null) applyFeatured(product, request.getFeatured());
        if (request.getPrice() != null) product.setPrice(request.getPrice());

        if (request.getProductTypeId() != null) {
            product.setProductType(resolveProductType(product.getStore(), request.getProductTypeId()));
        }

        if (hasFiles(images)) {
            product.setImageUrls(uploadImages(images));
        }
        if (request.getAffiliateUrl() != null) {
            product.setAffiliateUrl(resolveAffiliateUrl(product.getStore(), request.getAffiliateUrl(), product.getAffiliateUrl()));
        }
        if (request.getAttributes() != null) {
            Long productTypeId = product.getProductType() != null ? product.getProductType().getId() : null;
            product.setAttributes(attributeValidator.validateForUpdate(
                    product.getStore(), productTypeId, product.getAttributes(), request.getAttributes()));
        }

        return productRepository.save(product);
    }

    /**
     * Valida e retorna a URL de afiliado a persistir.
     * - Lojas AFFILIATE: campo obrigatório em creates (current == null), deve começar com "https://".
     * - Lojas STANDARD: campo ignorado — retorna sempre null para evitar dados órfãos.
     */
    private String resolveAffiliateUrl(Store store, String incoming, String current) {
        if (store.getProfileType() != StoreProfileType.AFFILIATE) return null;
        if (incoming == null || incoming.isBlank()) {
            if (current != null) return current;
            throw new BusinessRuleException("AFFILIATE_URL_REQUIRED",
                    "Affiliate stores require an affiliate URL for every product.");
        }
        if (!incoming.startsWith("https://")) {
            throw new BusinessRuleException("INVALID_AFFILIATE_URL",
                    "Affiliate URL must start with 'https://'.");
        }
        return incoming;
    }

    private ProductType resolveProductType(Store store, Long productTypeId) {
        if (productTypeId == null) {
            return null;
        }
        ProductType productType = productTypeRepository.findById(productTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductType", productTypeId));
        if (!productType.getStore().getId().equals(store.getId())) {
            throw new AccessDeniedException("This product type does not belong to your store.");
        }
        return productType;
    }

    private boolean hasFiles(List<MultipartFile> images) {
        return images != null && images.stream().anyMatch(file -> file != null && !file.isEmpty());
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        List<MultipartFile> nonEmpty = images.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
        if (nonEmpty.size() > MAX_IMAGES) {
            throw new BusinessRuleException("TOO_MANY_IMAGES",
                    "A product can have at most " + MAX_IMAGES + " images.");
        }
        return new ArrayList<>(nonEmpty.stream().map(storageService::uploadFile).toList());
    }

    private List<String> nonNullList(List<String> values) {
        return values != null ? new ArrayList<>(values) : new ArrayList<>();
    }

    @Override
    public Product toggleVisibility(Long id) {
        Product product = findById(id);
        assertProductOwnership(product);
        product.setIsVisible(!product.getIsVisible());
        return productRepository.save(product);
    }

    @Override
    public Product toggleFeatured(Long id) {
        Product product = findById(id);
        assertProductOwnership(product);
        applyFeatured(product, !Boolean.TRUE.equals(product.getFeatured()));
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findByStoreId(UUID storeId, int page, int size) {
        assertStoreOwnership(storeId);
        return productRepository.findByStoreId(storeId,
                PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findVisibleByStoreId(UUID storeId, int page, int size) {
        return productRepository.findByStoreIdAndIsVisibleTrueAndStoreIsActiveTrue(storeId,
                PageRequest.of(page, size, Sort.by("featured").descending().and(Sort.by("id").descending())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findFeaturedByStoreId(UUID storeId) {
        return productRepository.findByStoreIdAndFeaturedTrueAndStoreIsActiveTrue(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> search(UUID storeId, ProductFilter filter, int page, int size) {
        Specification<Product> spec = ProductSpec.fromStore(storeId)
                .and(ProductSpec.isVisible())
                .and(ProductSpec.storeIsActive())
                .and(filter.getKeyword() != null ? ProductSpec.nameContains(filter.getKeyword()) : null)
                .and(filter.getMinPrice() != null ? ProductSpec.minPrice(filter.getMinPrice()) : null)
                .and(filter.getMaxPrice() != null ? ProductSpec.maxPrice(filter.getMaxPrice()) : null)
                .and(filter.getProductTypeId() != null ? ProductSpec.hasProductType(filter.getProductTypeId()) : null);

        for (Specification<Product> attributeSpec
                : attributeFilterBuilder.build(storeId, filter.getProductTypeId(), filter.getAttributes())) {
            spec = spec.and(attributeSpec);
        }

        return productRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by("featured").descending().and(Sort.by("id").descending())));
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Override
    public void reorder(List<Long> productIds) {
        Store current = currentStoreResolver.getCurrentStore();

        List<Product> storeProducts = productRepository.findByStoreId(current.getId());
        if (productIds.size() != storeProducts.size()) {
            throw new BusinessRuleException("REORDER_INCOMPLETE",
                    "The list must contain all " + storeProducts.size() + " products of the store.");
        }

        Set<Long> storeProductIds = storeProducts.stream()
                .map(Product::getId)
                .collect(Collectors.toSet());

        for (Long id : productIds) {
            if (!storeProductIds.contains(id)) {
                throw new AccessDeniedException("Product " + id + " does not belong to your store.");
            }
        }

        String idsArray = "{" + productIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + "}";
        productRepository.bulkUpdateSortOrder(idsArray, current.getId().toString());
    }

    @Override
    public void delete(Long id) {
        Product product = findById(id);
        assertProductOwnership(product);
        productRepository.deleteById(id);
    }

    @Override
    public long registerWhatsappClick(Long productId) {
        Product product = findById(productId);
        whatsappClickRepository.save(new WhatsappClick(product));
        return whatsappClickRepository.countByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getClickCount(Long productId) {
        return whatsappClickRepository.countByProductId(productId);
    }

    @Override
    public long registerAffiliateClick(Long productId) {
        Product product = findById(productId);
        if (product.getStore().getProfileType() != StoreProfileType.AFFILIATE) {
            throw new BusinessRuleException("NOT_AFFILIATE_STORE",
                    "This product does not belong to an affiliate store.");
        }
        affiliateClickRepository.save(new AffiliateClick(product));
        return affiliateClickRepository.countByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getAffiliateClickCount(Long productId) {
        return affiliateClickRepository.countByProductId(productId);
    }

    private void assertStoreOwnership(Store store) {
        Store current = currentStoreResolver.getCurrentStore();
        if (!store.getId().equals(current.getId())) {
            throw new AccessDeniedException("You do not have permission to access this store.");
        }
    }

    private void assertStoreOwnership(UUID storeId) {
        Store current = currentStoreResolver.getCurrentStore();
        if (!storeId.equals(current.getId())) {
            throw new AccessDeniedException("You do not have permission to access this store.");
        }
    }

    private void assertProductOwnership(Product product) {
        Store current = currentStoreResolver.getCurrentStore();
        if (!product.getStore().getId().equals(current.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this product.");
        }
    }

    private void applyFeatured(Product product, boolean newValue) {
        if (newValue && !Boolean.TRUE.equals(product.getFeatured())) {
            long count = productRepository.countByStoreIdAndFeaturedTrue(product.getStore().getId());
            if (count >= MAX_FEATURED) {
                throw new BusinessRuleException("FEATURED_LIMIT_EXCEEDED",
                        "Featured product limit of " + MAX_FEATURED + " reached for this store.");
            }
        }
        product.setFeatured(newValue);
    }
}
