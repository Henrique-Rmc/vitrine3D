package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.RecurringFrequency;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecurringExpenseRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private RecurringFrequency frequency;

    @Min(1)
    @Max(28)
    private int dueDay;

    @Min(1)
    private int alertDaysBefore = 7;

    @NotNull
    private LocalDate nextDueDate;
}
