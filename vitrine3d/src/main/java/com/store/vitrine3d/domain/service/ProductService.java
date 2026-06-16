package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.rest.dto.ProductCreateRequest;
import com.store.vitrine3d.rest.dto.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    Product save(ProductCreateRequest request, MultipartFile image);
    Product update(Long id, ProductUpdateRequest request, MultipartFile image);
    Product toggleVisibility(Long id);
    Product toggleFeatured(Long id);
    Page<Product> findByStoreId(Long storeId, int page, int size);
    Page<Product> findVisibleByStoreId(Long storeId, int page, int size);
    List<Product> findFeaturedByStoreId(Long storeId);
    Product findById(Long id);
    void delete(Long id);
    long registerWhatsappClick(Long productId);
    long getClickCount(Long productId);
}
