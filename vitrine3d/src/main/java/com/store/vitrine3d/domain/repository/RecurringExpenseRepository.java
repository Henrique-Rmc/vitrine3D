package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, UUID> {

    List<RecurringExpense> findByStoreIdAndIsActiveTrueOrderByNameAsc(UUID storeId);

    Optional<RecurringExpense> findByIdAndStoreId(UUID id, UUID storeId);

    @Query("SELECT r FROM RecurringExpense r WHERE r.store.id = :storeId " +
           "AND r.isActive = true AND r.nextDueDate <= :alertThreshold")
    List<RecurringExpense> findDueAlerts(@Param("storeId") UUID storeId,
                                         @Param("alertThreshold") LocalDate alertThreshold);
}
