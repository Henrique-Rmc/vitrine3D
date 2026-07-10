package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.BusinessType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessTypeRepository extends JpaRepository<BusinessType, Long> {
    boolean existsBySlug(String slug);
}
