package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.PaymentMethod;
import com.store.vitrine3d.domain.model.PdvCreditPayment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PdvCreditPaymentResponse {
    private Long id;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private Instant paidAt;
    private Instant createdAt;

    public static PdvCreditPaymentResponse from(PdvCreditPayment p) {
        PdvCreditPaymentResponse dto = new PdvCreditPaymentResponse();
        dto.setId(p.getId());
        dto.setAmount(p.getAmount());
        dto.setPaymentMethod(p.getPaymentMethod());
        dto.setPaidAt(p.getPaidAt());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}
