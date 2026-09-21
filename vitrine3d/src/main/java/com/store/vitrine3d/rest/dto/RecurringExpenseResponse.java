package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.RecurringExpense;
import com.store.vitrine3d.domain.model.RecurringFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class RecurringExpenseResponse {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal amount;
    private RecurringFrequency frequency;
    private int dueDay;
    private int alertDaysBefore;
    private LocalDate nextDueDate;
    private LocalDate lastPaidDate;
    private boolean active;
    private long daysUntilDue;

    public static RecurringExpenseResponse from(RecurringExpense expense) {
        RecurringExpenseResponse dto = new RecurringExpenseResponse();
        dto.id = expense.getId();
        dto.name = expense.getName();
        dto.description = expense.getDescription();
        dto.amount = expense.getAmount();
        dto.frequency = expense.getFrequency();
        dto.dueDay = expense.getDueDay();
        dto.alertDaysBefore = expense.getAlertDaysBefore();
        dto.nextDueDate = expense.getNextDueDate();
        dto.lastPaidDate = expense.getLastPaidDate();
        dto.active = expense.isActive();
        dto.daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), expense.getNextDueDate());
        return dto;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public RecurringFrequency getFrequency() { return frequency; }
    public int getDueDay() { return dueDay; }
    public int getAlertDaysBefore() { return alertDaysBefore; }
    public LocalDate getNextDueDate() { return nextDueDate; }
    public LocalDate getLastPaidDate() { return lastPaidDate; }
    public boolean isActive() { return active; }
    public long getDaysUntilDue() { return daysUntilDue; }
}
