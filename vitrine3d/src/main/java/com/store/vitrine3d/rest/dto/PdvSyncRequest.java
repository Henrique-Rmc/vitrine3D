package com.store.vitrine3d.rest.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PdvSyncRequest {
    private List<@Valid PdvSaleRequest> sales = new ArrayList<>();
    private List<@Valid PdvCashFlowRequest> cashFlows = new ArrayList<>();
}
