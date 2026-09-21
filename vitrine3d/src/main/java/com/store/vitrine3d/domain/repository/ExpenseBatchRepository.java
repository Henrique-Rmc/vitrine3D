package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.ExpenseBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseBatchRepository extends JpaRepository<ExpenseBatch, UUID> {

    boolean existsByStoreIdAndOfflineId(UUID storeId, String offlineId);

    Optional<ExpenseBatch> findByIdAndStoreId(UUID id, UUID storeId);

    Page<ExpenseBatch> findByStoreIdAndBatchDateBetweenOrderByBatchDateDesc(
            UUID storeId, Instant from, Instant to, Pageable pageable);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM ExpenseBatch b " +
           "WHERE b.store.id = :storeId AND b.batchDate >= :from AND b.batchDate < :to")
    BigDecimal sumTotalByPeriod(@Param("storeId") UUID storeId,
                                @Param("from") Instant from,
                                @Param("to") Instant to);

    @Modifying
    @Query("DELETE FROM ExpenseBatch b WHERE b.store.id = :storeId")
    void deleteAllByStoreId(@Param("storeId") UUID storeId);
}
