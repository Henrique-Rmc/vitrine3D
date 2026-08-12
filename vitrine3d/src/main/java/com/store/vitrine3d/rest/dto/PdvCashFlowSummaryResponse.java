package com.store.vitrine3d.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PdvCashFlowSummaryResponse {
    private BigDecimal totalIn;
    private BigDecimal totalOut;
    private BigDecimal balance;
    private long countIn;
    private long countOut;
}
