package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Category;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.CategoryRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.ProductService;
import com.store.vitrine3d.infrastructure.storage.StorageService;
import com.store.vitrine3d.rest.dto.ProductCreateRequest;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final StorageService storageService;

    public ProductServiceImpl(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               StoreRepository storeRepository,
                               StorageService storageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
        this.storageService = storageService;
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
        product.setMulticolor(request.getMulticolor() != null ? request.getMulticolor() : false);
        product.setDimensions(request.getDimensions());
        product.setImageUrl(imageUrl);
        product.setCategory(category);
        product.setStore(store);

        return productRepository.save(product);
    }

    @Override
    public List<Product> findByStoreId(Long storeId) {
        return productRepository.findByStoreId(storeId);
    }

    @Override
    public List<Product> findVisibleByStoreId(Long storeId) {
        return productRepository.findByStoreIdAndIsVisibleTrue(storeId);
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));
    }

    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}
