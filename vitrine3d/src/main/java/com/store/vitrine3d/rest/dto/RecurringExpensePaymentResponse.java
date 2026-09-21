package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.RecurringExpensePayment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class RecurringExpensePaymentResponse {

    private UUID id;
    private UUID recurringExpenseId;
    private String recurringExpenseName;
    private BigDecimal amount;
    private Instant paidAt;
    private String note;
    private List<String> imageUrls;
    private Instant createdAt;

    public static RecurringExpensePaymentResponse from(RecurringExpensePayment payment) {
        RecurringExpensePaymentResponse dto = new RecurringExpensePaymentResponse();
        dto.setId(payment.getId());
        dto.setRecurringExpenseId(payment.getRecurringExpense().getId());
        dto.setRecurringExpenseName(payment.getRecurringExpense().getName());
        dto.setAmount(payment.getAmount());
        dto.setPaidAt(payment.getPaidAt());
        dto.setNote(payment.getNote());
        dto.setImageUrls(List.copyOf(payment.getImageUrls()));
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}
