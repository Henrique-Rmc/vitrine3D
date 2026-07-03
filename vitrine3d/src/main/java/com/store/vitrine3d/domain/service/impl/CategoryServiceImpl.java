package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Category;
import com.store.vitrine3d.domain.repository.CategoryRepository;
import com.store.vitrine3d.domain.service.CategoryService;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl
        extends AbstractStoreMetadataService<Category>
        implements CategoryService {

    public CategoryServiceImpl(CategoryRepository repository, CurrentStoreResolver currentStoreResolver) {
        super(repository, currentStoreResolver);
    }

    @Override
    protected Category newInstance() { return new Category(); }

    @Override
    protected String resourceName() { return "Category"; }
}
