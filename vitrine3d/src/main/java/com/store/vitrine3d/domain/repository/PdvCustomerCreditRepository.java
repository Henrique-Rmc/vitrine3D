package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.CreditStatus;
import com.store.vitrine3d.domain.model.PdvCustomerCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PdvCustomerCreditRepository extends JpaRepository<PdvCustomerCredit, UUID> {
    List<PdvCustomerCredit> findByCustomerIdAndCustomerStoreIdOrderByCreatedAtDesc(UUID customerId, UUID storeId);
    Optional<PdvCustomerCredit> findByIdAndCustomerStoreId(UUID id, UUID storeId);
    boolean existsByCustomerIdAndStatusIn(UUID customerId, Collection<CreditStatus> statuses);
    Optional<PdvCustomerCredit> findByOriginSaleId(UUID originSaleId);

    @Query("SELECT COALESCE(SUM(c.totalDue - c.amountPaid), 0) FROM PdvCustomerCredit c " +
           "WHERE c.customer.store.id = :storeId " +
           "AND c.status <> com.store.vitrine3d.domain.model.CreditStatus.PAID")
    BigDecimal sumOpenBalanceByStore(@Param("storeId") UUID storeId);

    @Modifying
    @Query("DELETE FROM PdvCreditPayment p WHERE p.credit.customer.store.id = :storeId")
    void deletePaymentsByStoreId(@Param("storeId") UUID storeId);

    @Modifying
    @Query("DELETE FROM PdvCustomerCredit c WHERE c.customer.store.id = :storeId")
    void deleteAllByCustomerStoreId(@Param("storeId") UUID storeId);
}
