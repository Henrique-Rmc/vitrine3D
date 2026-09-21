package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.PdvCustomerRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PdvCustomerRelationshipRepository extends JpaRepository<PdvCustomerRelationship, Long> {

    // Retorna todos os vínculos onde o cliente é origem OU destino
    @Query("SELECT r FROM PdvCustomerRelationship r " +
           "WHERE r.store.id = :storeId " +
           "AND (r.customer.id = :customerId OR r.relative.id = :customerId) " +
           "ORDER BY r.createdAt DESC")
    List<PdvCustomerRelationship> findAllByCustomerInvolved(@Param("storeId") UUID storeId,
                                                             @Param("customerId") UUID customerId);

    boolean existsByCustomerIdAndRelativeId(UUID customerId, UUID relativeId);

    Optional<PdvCustomerRelationship> findByIdAndStoreId(Long id, UUID storeId);
}
