package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.PdvEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PdvEmployeeRepository extends JpaRepository<PdvEmployee, UUID> {
    List<PdvEmployee> findByStoreIdOrderByNameAsc(UUID storeId);
    Optional<PdvEmployee> findByIdAndStoreId(UUID id, UUID storeId);

    @Modifying
    @Query("DELETE FROM PdvEmployee e WHERE e.store.id = :storeId")
    void deleteAllByStoreId(@Param("storeId") UUID storeId);
}
