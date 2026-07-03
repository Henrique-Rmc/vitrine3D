package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Material;
import com.store.vitrine3d.domain.repository.MaterialRepository;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import com.store.vitrine3d.domain.service.MaterialService;
import org.springframework.stereotype.Service;

@Service
public class MaterialServiceImpl
        extends AbstractStoreMetadataService<Material>
        implements MaterialService {

    public MaterialServiceImpl(MaterialRepository repository, CurrentStoreResolver currentStoreResolver) {
        super(repository, currentStoreResolver);
    }

    @Override
    protected Material newInstance() { return new Material(); }

    @Override
    protected String resourceName() { return "Material"; }
}
