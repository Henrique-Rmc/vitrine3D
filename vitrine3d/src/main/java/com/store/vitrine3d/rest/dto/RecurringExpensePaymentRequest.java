package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class RecurringExpensePaymentRequest {

    /** Valor efetivamente pago. Se omitido, usa o valor previsto na definição da conta. */
    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;

    /** Data do pagamento. Se omitida, assume agora. */
    private Instant paidAt;

    @Size(max = 500)
    private String note;
}
