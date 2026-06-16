package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.WhatsappClick;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhatsappClickRepository extends JpaRepository<WhatsappClick, Long> {
    long countByProductId(Long productId);
}
