package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Um pagamento de uma conta fixa. A RecurringExpense é a definição (nome, valor previsto,
 * periodicidade) e se sobrescreve a cada mês; o pagamento é o evento, e é nele que ficam o
 * valor efetivamente pago e os comprovantes.
 */
@Entity
@Table(name = "recurring_expense_payments")
public class RecurringExpensePayment {

    /** Máximo de comprovantes por pagamento (ex.: boleto + comprovante da transferência). */
    public static final int MAX_IMAGES = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recurring_expense_id", nullable = false)
    private RecurringExpense recurringExpense;

    /** Valor real daquele mês — pode divergir do amount previsto na definição. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt;

    @Column(length = 500)
    private String note;

    /** Referência ao lançamento de caixa gerado (sem FK: o estorno não apaga o histórico). */
    @Column(name = "cash_flow_id")
    private UUID cashFlowId;

    @ElementCollection
    @CollectionTable(name = "recurring_expense_payment_images",
            joinColumns = @JoinColumn(name = "payment_id"))
    @Column(name = "image_url", nullable = false, length = 2048)
    @OrderColumn(name = "image_order")
    private List<String> imageUrls = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public RecurringExpensePayment() {}

    public UUID getId() { return id; }

    public RecurringExpense getRecurringExpense() { return recurringExpense; }
    public void setRecurringExpense(RecurringExpense recurringExpense) { this.recurringExpense = recurringExpense; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public UUID getCashFlowId() { return cashFlowId; }
    public void setCashFlowId(UUID cashFlowId) { this.cashFlowId = cashFlowId; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public Instant getCreatedAt() { return createdAt; }
}
