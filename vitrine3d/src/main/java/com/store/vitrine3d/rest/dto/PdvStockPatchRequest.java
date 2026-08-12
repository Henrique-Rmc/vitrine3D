package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PdvStockPatchRequest {
    @NotNull
    private Integer quantityDelta;

    private Boolean trackStock;
}
