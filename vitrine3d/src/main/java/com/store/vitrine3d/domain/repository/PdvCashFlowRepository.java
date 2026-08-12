package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.FlowType;
import com.store.vitrine3d.domain.model.PdvCashFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface PdvCashFlowRepository extends JpaRepository<PdvCashFlow, UUID> {
    boolean existsByStoreIdAndOfflineId(UUID storeId, String offlineId);
    Page<PdvCashFlow> findByStoreIdAndFlowDateBetweenOrderByFlowDateDesc(UUID storeId, Instant from, Instant to, Pageable pageable);

    @Modifying
    @Query("DELETE FROM PdvCashFlow cf WHERE cf.store.id = :storeId")
    void deleteAllByStoreId(@Param("storeId") UUID storeId);

    @Query("SELECT COALESCE(SUM(cf.amount), 0) FROM PdvCashFlow cf WHERE cf.store.id = :storeId AND cf.type = :type AND cf.flowDate >= :from AND cf.flowDate < :to")
    BigDecimal sumAmountByTypeAndPeriod(@Param("storeId") UUID storeId, @Param("type") FlowType type, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COUNT(cf) FROM PdvCashFlow cf WHERE cf.store.id = :storeId AND cf.type = :type AND cf.flowDate >= :from AND cf.flowDate < :to")
    long countByTypeAndPeriod(@Param("storeId") UUID storeId, @Param("type") FlowType type, @Param("from") Instant from, @Param("to") Instant to);
}
