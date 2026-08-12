package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.FlowCategory;
import com.store.vitrine3d.domain.model.FlowType;
import com.store.vitrine3d.domain.model.PdvCashFlow;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class PdvCashFlowResponse {
    private UUID id;
    private String offlineId;
    private UUID operatorId;
    private String operatorName;
    private FlowType type;
    private FlowCategory category;
    private BigDecimal amount;
    private String description;
    private Instant flowDate;
    private Instant syncedAt;

    public static PdvCashFlowResponse from(PdvCashFlow flow) {
        PdvCashFlowResponse dto = new PdvCashFlowResponse();
        dto.setId(flow.getId());
        dto.setOfflineId(flow.getOfflineId());
        if (flow.getOperator() != null) {
            dto.setOperatorId(flow.getOperator().getId());
            dto.setOperatorName(flow.getOperator().getName());
        }
        dto.setType(flow.getType());
        dto.setCategory(flow.getCategory());
        dto.setAmount(flow.getAmount());
        dto.setDescription(flow.getDescription());
        dto.setFlowDate(flow.getFlowDate());
        dto.setSyncedAt(flow.getSyncedAt());
        return dto;
    }
}
