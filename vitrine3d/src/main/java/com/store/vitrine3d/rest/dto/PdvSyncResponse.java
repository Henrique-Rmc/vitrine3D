package com.store.vitrine3d.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PdvSyncResponse {
    private int salesProcessed;
    private int salesSkipped;
    private int cashFlowsProcessed;
    private int cashFlowsSkipped;
    private List<String> errors;
}
