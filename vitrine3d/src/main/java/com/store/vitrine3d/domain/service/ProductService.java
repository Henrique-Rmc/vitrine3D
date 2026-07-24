package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.rest.dto.ProductCreateRequest;
import com.store.vitrine3d.rest.dto.ProductFilter;
import com.store.vitrine3d.rest.dto.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    Product save(ProductCreateRequest request, List<MultipartFile> images);
    Product update(Long id, ProductUpdateRequest request, List<MultipartFile> images);
    Product toggleVisibility(Long id);
    Product toggleFeatured(Long id);
    Page<Product> findByStoreId(UUID storeId, int page, int size);
    Page<Product> findVisibleByStoreId(UUID storeId, int page, int size);
    List<Product> findFeaturedByStoreId(UUID storeId);
    Page<Product> search(UUID storeId, ProductFilter filter, int page, int size);
    Product findById(Long id);
    void delete(Long id);
    void reorder(List<Long> productIds);
    long registerWhatsappClick(Long productId);
    long getClickCount(Long productId);
    long registerAffiliateClick(Long productId);
    long getAffiliateClickCount(Long productId);
}
