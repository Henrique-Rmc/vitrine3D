package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.PlanLimit;
import com.store.vitrine3d.domain.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// DORMANT — not wired into the system yet.
public interface PlanLimitRepository extends JpaRepository<PlanLimit, Long> {
    Optional<PlanLimit> findByPlan(SubscriptionPlan plan);
    boolean existsByPlan(SubscriptionPlan plan);
}
