package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Category;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.CategoryRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.CategoryService;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, StoreRepository storeRepository) {
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public List<Category> findPublic() {
        return categoryRepository.findByIsGlobalTrue();
    }

    @Override
    public List<Category> findByStore(UUID storeId) {
        return categoryRepository.findByIsGlobalTrueOrStoreId(storeId);
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
    }

    @Override
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category update(Long id, String name) {
        Category category = findById(id);
        assertCategoryOwnership(category);
        category.setName(name);
        return categoryRepository.save(category);
    }

    @Override
    public void delete(Long id) {
        Category category = findById(id);
        assertCategoryOwnership(category);
        categoryRepository.deleteById(id);
    }

    private Store getCurrentStore() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return storeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found after authentication"));
    }

    private void assertCategoryOwnership(Category category) {
        if (Boolean.TRUE.equals(category.getIsGlobal())) {
            throw new AccessDeniedException("Global categories cannot be modified.");
        }
        Store current = getCurrentStore();
        if (category.getStore() == null || !category.getStore().getId().equals(current.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this category.");
        }
    }
}
