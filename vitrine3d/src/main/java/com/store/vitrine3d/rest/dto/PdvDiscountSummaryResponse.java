package com.store.vitrine3d.rest.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PdvDiscountSummaryResponse {
    private BigDecimal totalOriginal;
    private BigDecimal totalCharged;
    private BigDecimal totalDiscounted;
}
