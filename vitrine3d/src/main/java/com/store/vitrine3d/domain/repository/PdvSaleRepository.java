package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.PdvSale;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PdvSaleRepository extends JpaRepository<PdvSale, UUID> {
    boolean existsByStoreIdAndOfflineId(UUID storeId, String offlineId);
    Optional<PdvSale> findByIdAndStoreId(UUID id, UUID storeId);
    Page<PdvSale> findByStoreIdAndSaleDateBetweenOrderBySaleDateDesc(UUID storeId, Instant from, Instant to, Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.originalAmount), 0) FROM PdvSale s " +
           "WHERE s.store.id = :storeId AND s.saleDate >= :from AND s.saleDate < :to " +
           "AND s.originalAmount IS NOT NULL AND s.status <> com.store.vitrine3d.domain.model.SaleStatus.CANCELLED")
    BigDecimal sumOriginalAmountByPeriod(@Param("storeId") UUID storeId,
                                         @Param("from") Instant from,
                                         @Param("to") Instant to);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM PdvSale s " +
           "WHERE s.store.id = :storeId AND s.saleDate >= :from AND s.saleDate < :to " +
           "AND s.originalAmount IS NOT NULL AND s.status <> com.store.vitrine3d.domain.model.SaleStatus.CANCELLED")
    BigDecimal sumChargedAmountForDiscountedByPeriod(@Param("storeId") UUID storeId,
                                                     @Param("from") Instant from,
                                                     @Param("to") Instant to);

    // ── Balanço (regime de competência: exclui vendas canceladas) ─────────────

    // Receita bruta é calculada no nível do item: PdvSale.originalAmount é null quando
    // não houve desconto, então somá-lo perderia todas as vendas sem desconto.
    @Query("SELECT COALESCE(SUM(COALESCE(i.originalUnitPrice, i.unitPrice) * i.quantity), 0) " +
           "FROM PdvSaleItem i WHERE i.sale.store.id = :storeId " +
           "AND i.sale.saleDate >= :from AND i.sale.saleDate < :to " +
           "AND i.sale.status <> com.store.vitrine3d.domain.model.SaleStatus.CANCELLED")
    BigDecimal sumGrossRevenueByPeriod(@Param("storeId") UUID storeId,
                                       @Param("from") Instant from,
                                       @Param("to") Instant to);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM PdvSale s " +
           "WHERE s.store.id = :storeId AND s.saleDate >= :from AND s.saleDate < :to " +
           "AND s.status <> com.store.vitrine3d.domain.model.SaleStatus.CANCELLED")
    BigDecimal sumNetRevenueByPeriod(@Param("storeId") UUID storeId,
                                     @Param("from") Instant from,
                                     @Param("to") Instant to);

    @Query("SELECT COUNT(s) FROM PdvSale s " +
           "WHERE s.store.id = :storeId AND s.saleDate >= :from AND s.saleDate < :to " +
           "AND s.status <> com.store.vitrine3d.domain.model.SaleStatus.CANCELLED")
    long countSalesByPeriod(@Param("storeId") UUID storeId,
                            @Param("from") Instant from,
                            @Param("to") Instant to);

    @Query("SELECT COALESCE(SUM(i.unitCost * i.quantity), 0) FROM PdvSaleItem i " +
           "WHERE i.sale.store.id = :storeId " +
           "AND i.sale.saleDate >= :from AND i.sale.saleDate < :to " +
           "AND i.sale.status <> com.store.vitrine3d.domain.model.SaleStatus.CANCELLED " +
           "AND i.unitCost IS NOT NULL")
    BigDecimal sumCogsByPeriod(@Param("storeId") UUID storeId,
                               @Param("from") Instant from,
                               @Param("to") Instant to);

    // Os dois sums abaixo medem a cobertura do CMV: quanto da receita tem custo conhecido.
    @Query("SELECT COALESCE(SUM(i.subtotal), 0) FROM PdvSaleItem i " +
           "WHERE i.sale.store.id = :storeId " +
           "AND i.sale.saleDate >= :from AND i.sale.saleDate < :to " +
           "AND i.sale.status <> com.store.vitrine3d.domain.model.SaleStatus.CANCELLED " +
           "AND i.unitCost IS NOT NULL")
    BigDecimal sumSubtotalWithCostByPeriod(@Param("storeId") UUID storeId,
                                           @Param("from") Instant from,
                                           @Param("to") Instant to);

    @Query("SELECT COALESCE(SUM(i.subtotal), 0) FROM PdvSaleItem i " +
           "WHERE i.sale.store.id = :storeId " +
           "AND i.sale.saleDate >= :from AND i.sale.saleDate < :to " +
           "AND i.sale.status <> com.store.vitrine3d.domain.model.SaleStatus.CANCELLED")
    BigDecimal sumSubtotalAllByPeriod(@Param("storeId") UUID storeId,
                                      @Param("from") Instant from,
                                      @Param("to") Instant to);

    @Modifying
    @Query("DELETE FROM PdvSaleItem i WHERE i.sale.store.id = :storeId")
    void deleteItemsByStoreId(@Param("storeId") UUID storeId);

    @Modifying
    @Query("DELETE FROM PdvSale s WHERE s.store.id = :storeId")
    void deleteAllByStoreId(@Param("storeId") UUID storeId);
}
