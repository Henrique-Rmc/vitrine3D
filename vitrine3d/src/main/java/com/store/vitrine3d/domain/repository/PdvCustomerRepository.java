package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.PdvCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PdvCustomerRepository extends JpaRepository<PdvCustomer, UUID> {
    List<PdvCustomer> findByStoreIdOrderByNameAsc(UUID storeId);
    Optional<PdvCustomer> findByIdAndStoreId(UUID id, UUID storeId);

    @Modifying
    @Query("DELETE FROM PdvCustomer c WHERE c.store.id = :storeId")
    void deleteAllByStoreId(@Param("storeId") UUID storeId);
}
