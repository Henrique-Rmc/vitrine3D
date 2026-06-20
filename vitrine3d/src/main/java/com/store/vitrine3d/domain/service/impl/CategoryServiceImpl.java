package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Category;
import com.store.vitrine3d.domain.repository.CategoryRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl
        extends AbstractStoreMetadataService<Category>
        implements CategoryService {

    public CategoryServiceImpl(CategoryRepository repository, StoreRepository storeRepository) {
        super(repository, storeRepository);
    }

    @Override
    protected Category newInstance() { return new Category(); }

    @Override
    protected String resourceName() { return "Category"; }
}
