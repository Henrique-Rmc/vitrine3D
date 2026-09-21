package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.ExpenseBatch;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ExpenseBatchResponse {

    private UUID id;
    private String offlineId;
    private Instant batchDate;
    private BigDecimal totalAmount;
    private String note;
    private UUID operatorId;
    private Instant syncedAt;
    private List<ExpenseBatchItemResponse> items;

    public static ExpenseBatchResponse from(ExpenseBatch batch) {
        ExpenseBatchResponse dto = new ExpenseBatchResponse();
        dto.id = batch.getId();
        dto.offlineId = batch.getOfflineId();
        dto.batchDate = batch.getBatchDate();
        dto.totalAmount = batch.getTotalAmount();
        dto.note = batch.getNote();
        dto.operatorId = batch.getOperator() != null ? batch.getOperator().getId() : null;
        dto.syncedAt = batch.getSyncedAt();
        dto.items = batch.getItems().stream().map(ExpenseBatchItemResponse::from).toList();
        return dto;
    }

    public UUID getId() { return id; }
    public String getOfflineId() { return offlineId; }
    public Instant getBatchDate() { return batchDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getNote() { return note; }
    public UUID getOperatorId() { return operatorId; }
    public Instant getSyncedAt() { return syncedAt; }
    public List<ExpenseBatchItemResponse> getItems() { return items; }
}
