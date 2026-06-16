package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Category;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.WhatsappClick;
import com.store.vitrine3d.domain.repository.CategoryRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.repository.WhatsappClickRepository;
import com.store.vitrine3d.domain.service.ProductService;
import com.store.vitrine3d.infrastructure.storage.StorageService;
import com.store.vitrine3d.rest.dto.ProductCreateRequest;
import com.store.vitrine3d.rest.dto.ProductUpdateRequest;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final int MAX_FEATURED = 3;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final StorageService storageService;
    private final WhatsappClickRepository whatsappClickRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               StoreRepository storeRepository,
                               StorageService storageService,
                               WhatsappClickRepository whatsappClickRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
        this.storageService = storageService;
        this.whatsappClickRepository = whatsappClickRepository;
    }

    @Override
    public Product save(ProductCreateRequest request, MultipartFile image) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", request.getCategoryId()));

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Loja", request.getStoreId()));

        String imageUrl = (image != null && !image.isEmpty()) ? storageService.uploadFile(image) : null;

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setMaterial(request.getMaterial());
        product.setMulticolor(Boolean.TRUE.equals(request.getMulticolor()));
        product.setDimensions(request.getDimensions());
        product.setImageUrl(imageUrl);
        product.setCategory(category);
        product.setStore(store);

        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, ProductUpdateRequest request, MultipartFile image) {
        Product product = findById(id);

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getMaterial() != null) product.setMaterial(request.getMaterial());
        if (request.getMulticolor() != null) product.setMulticolor(Boolean.TRUE.equals(request.getMulticolor()));
        if (request.getDimensions() != null) product.setDimensions(request.getDimensions());
        if (request.getIsVisible() != null) product.setIsVisible(request.getIsVisible());

        if (request.getFeatured() != null) {
            applyFeatured(product, request.getFeatured());
        }

        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria", request.getCategoryId())));
        }

        if (image != null && !image.isEmpty()) {
            product.setImageUrl(storageService.uploadFile(image));
        }

        return productRepository.save(product);
    }

    @Override
    public Product toggleVisibility(Long id) {
        Product product = findById(id);
        product.setIsVisible(!product.getIsVisible());
        return productRepository.save(product);
    }

    @Override
    public Product toggleFeatured(Long id) {
        Product product = findById(id);
        applyFeatured(product, !Boolean.TRUE.equals(product.getFeatured()));
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findByStoreId(Long storeId, int page, int size) {
        return productRepository.findByStoreId(storeId,
                PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findVisibleByStoreId(Long storeId, int page, int size) {
        return productRepository.findByStoreIdAndIsVisibleTrue(storeId,
                PageRequest.of(page, size, Sort.by("featured").descending().and(Sort.by("id").descending())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findFeaturedByStoreId(Long storeId) {
        return productRepository.findByStoreIdAndFeaturedTrue(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));
    }

    @Override
    public void delete(Long id) {
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

    private void applyFeatured(Product product, boolean newValue) {
        if (newValue && !Boolean.TRUE.equals(product.getFeatured())) {
            long count = productRepository.countByStoreIdAndFeaturedTrue(product.getStore().getId());
            if (count >= MAX_FEATURED) {
                throw new IllegalArgumentException(
                        "Limite de " + MAX_FEATURED + " produtos em destaque atingido para esta loja.");
            }
        }
        product.setFeatured(newValue);
    }
}
