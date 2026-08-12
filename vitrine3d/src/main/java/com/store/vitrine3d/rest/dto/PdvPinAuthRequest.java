package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PdvPinAuthRequest {
    @NotNull
    private UUID employeeId;

    @NotBlank
    private String pin;
}
