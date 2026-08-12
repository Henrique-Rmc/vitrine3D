package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.CreditStatus;
import com.store.vitrine3d.domain.model.PdvCustomerCredit;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class PdvCustomerCreditResponse {
    private UUID id;
    private UUID originSaleId;
    private BigDecimal totalDue;
    private BigDecimal amountPaid;
    private BigDecimal balance;
    private Instant dueDate;
    private CreditStatus status;
    private String note;
    private Instant createdAt;
    private List<PdvCreditPaymentResponse> payments;

    public static PdvCustomerCreditResponse from(PdvCustomerCredit credit) {
        PdvCustomerCreditResponse dto = new PdvCustomerCreditResponse();
        dto.setId(credit.getId());
        dto.setOriginSaleId(credit.getOriginSaleId());
        dto.setTotalDue(credit.getTotalDue());
        dto.setAmountPaid(credit.getAmountPaid());
        dto.setBalance(credit.getBalance());
        dto.setDueDate(credit.getDueDate());
        dto.setStatus(credit.getStatus());
        dto.setNote(credit.getNote());
        dto.setCreatedAt(credit.getCreatedAt());
        dto.setPayments(credit.getPayments().stream().map(PdvCreditPaymentResponse::from).toList());
        return dto;
    }
}
