package com.store.vitrine3d.domain.model;

public enum FlowCategory {
    SALE,
    CREDIT_PAYMENT,
    OPENING,
    CLOSING,
    EXPENSE,            // Gastos com insumos (ExpenseBatch)
    RECURRING_EXPENSE,  // Contas fixas (RecurringExpense) — separado para o balanço não contar duas vezes
    WITHDRAWAL,
    OTHER
}
