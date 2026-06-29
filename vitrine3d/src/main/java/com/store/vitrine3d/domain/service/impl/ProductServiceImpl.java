package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Category;
import com.store.vitrine3d.domain.model.Material;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.WhatsappClick;
import com.store.vitrine3d.domain.repository.CategoryRepository;
import com.store.vitrine3d.domain.repository.MaterialRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.repository.WhatsappClickRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final int MAX_FEATURED = 3;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;
    private final StoreRepository storeRepository;
    private final StorageService storageService;
    private final WhatsappClickRepository whatsappClickRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               MaterialRepository materialRepository,
                               StoreRepository storeRepository,
                               StorageService storageService,
                               WhatsappClickRepository whatsappClickRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.materialRepository = materialRepository;
        this.storeRepository = storeRepository;
        this.storageService = storageService;
        this.whatsappClickRepository = whatsappClickRepository;
    }

    @Override
    public Product save(ProductCreateRequest request, MultipartFile image) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store", request.getStoreId()));

        assertStoreOwnership(store);

        String imageUrl = (image != null && !image.isEmpty()) ? storageService.uploadFile(image) : null;

        Material material = request.getMaterialId() != null
                ? materialRepository.findById(request.getMaterialId())
                        .orElseThrow(() -> new ResourceNotFoundException("Material", request.getMaterialId()))
                : null;

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setMaterial(material);
        product.setDimensions(request.getDimensions());
        product.setPrice(request.getPrice());
        product.setImageUrl(imageUrl);
        product.setCategory(category);
        product.setStore(store);

        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, ProductUpdateRequest request, MultipartFile image) {
        Product product = findById(id);
        assertProductOwnership(product);

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getDimensions() != null) product.setDimensions(request.getDimensions());
        if (request.getIsVisible() != null) product.setIsVisible(request.getIsVisible());
        if (request.getFeatured() != null) applyFeatured(product, request.getFeatured());
        if (request.getPrice() != null) product.setPrice(request.getPrice());

        if (request.getMaterialId() != null) {
            product.setMaterial(materialRepository.findById(request.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material", request.getMaterialId())));
        }
        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId())));
        }
        if (image != null && !image.isEmpty()) {
            product.setImageUrl(storageService.uploadFile(image));
        }

        return productRepository.save(product);
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
        return productRepository.findByStoreIdAndIsVisibleTrue(storeId,
                PageRequest.of(page, size, Sort.by("featured").descending().and(Sort.by("id").descending())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findFeaturedByStoreId(UUID storeId) {
        return productRepository.findByStoreIdAndFeaturedTrue(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> search(UUID storeId, ProductFilter filter, int page, int size) {
        Specification<Product> spec = ProductSpec.fromStore(storeId)
                .and(ProductSpec.isVisible())
                .and(filter.getKeyword() != null ? ProductSpec.nameContains(filter.getKeyword()) : null)
                .and(filter.getCategoryId() != null ? ProductSpec.hasCategory(filter.getCategoryId()) : null)
                .and(filter.getMaterialId() != null ? ProductSpec.hasMaterial(filter.getMaterialId()) : null)
                .and(filter.getMinPrice() != null ? ProductSpec.minPrice(filter.getMinPrice()) : null)
                .and(filter.getMaxPrice() != null ? ProductSpec.maxPrice(filter.getMaxPrice()) : null);

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
        Store current = getCurrentStore();

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

    private Store getCurrentStore() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return storeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found after authentication"));
    }

    private void assertStoreOwnership(Store store) {
        Store current = getCurrentStore();
        if (!store.getId().equals(current.getId())) {
            throw new AccessDeniedException("You do not have permission to access this store.");
        }
    }

    private void assertStoreOwnership(UUID storeId) {
        Store current = getCurrentStore();
        if (!storeId.equals(current.getId())) {
            throw new AccessDeniedException("You do not have permission to access this store.");
        }
    }

    private void assertProductOwnership(Product product) {
        Store current = getCurrentStore();
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
