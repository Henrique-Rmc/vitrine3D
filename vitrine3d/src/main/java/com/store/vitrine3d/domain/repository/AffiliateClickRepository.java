package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.AffiliateClick;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffiliateClickRepository extends JpaRepository<AffiliateClick, Long> {
    long countByProductId(Long productId);
}
