package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.FlowCategory;
import com.store.vitrine3d.domain.model.FlowType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class PdvCashFlowRequest {
    @NotBlank
    private String offlineId;

    @NotNull
    private FlowType type;

    @NotNull
    private FlowCategory category;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount;

    private String description;

    @NotNull
    private Instant flowDate;

    private UUID operatorId;
}
