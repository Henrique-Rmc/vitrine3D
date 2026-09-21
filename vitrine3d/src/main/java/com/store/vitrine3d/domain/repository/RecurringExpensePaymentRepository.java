package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.RecurringExpensePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringExpensePaymentRepository extends JpaRepository<RecurringExpensePayment, UUID> {

    List<RecurringExpensePayment> findByRecurringExpenseIdOrderByPaidAtDesc(UUID recurringExpenseId);

    /** Escopo por loja navegando pela definição — o pagamento não guarda store_id. */
    @Query("SELECT p FROM RecurringExpensePayment p " +
           "WHERE p.id = :paymentId AND p.recurringExpense.store.id = :storeId")
    Optional<RecurringExpensePayment> findByIdAndStoreId(@Param("paymentId") UUID paymentId,
                                                          @Param("storeId") UUID storeId);

    /** Total pago de uma conta fixa específica no período. */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM RecurringExpensePayment p " +
           "WHERE p.recurringExpense.id = :recurringExpenseId " +
           "AND p.paidAt >= :from AND p.paidAt < :to")
    BigDecimal sumByExpenseAndPeriod(@Param("recurringExpenseId") UUID recurringExpenseId,
                                      @Param("from") Instant from,
                                      @Param("to") Instant to);
}
