package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.rest.dto.ProductCreateRequest;
import com.store.vitrine3d.rest.dto.ProductUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    Product save(ProductCreateRequest request, MultipartFile image);
    Product update(Long id, ProductUpdateRequest request, MultipartFile image);
    Product toggleVisibility(Long id);
    List<Product> findByStoreId(Long storeId);
    List<Product> findVisibleByStoreId(Long storeId);
    Product findById(Long id);
    void delete(Long id);
}
