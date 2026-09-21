package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExpenseStockPatchRequest {

    @NotNull
    private Integer quantityDelta;
}
