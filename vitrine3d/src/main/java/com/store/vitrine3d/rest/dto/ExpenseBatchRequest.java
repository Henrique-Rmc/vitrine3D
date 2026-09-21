package com.store.vitrine3d.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ExpenseBatchRequest {

    @NotBlank
    private String offlineId;

    @NotNull
    private Instant batchDate;

    private UUID operatorId;

    private String note;

    @NotEmpty
    @Valid
    private List<ExpenseBatchItemRequest> items;
}
