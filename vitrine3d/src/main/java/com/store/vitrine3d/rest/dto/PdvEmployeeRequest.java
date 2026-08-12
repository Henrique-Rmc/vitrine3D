package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.PdvEmployeeRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PdvEmployeeRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(min = 4, max = 6, message = "PIN deve ter entre 4 e 6 dígitos")
    @Pattern(regexp = "\\d*", message = "PIN deve conter apenas dígitos")
    private String pin;

    private PdvEmployeeRole role = PdvEmployeeRole.CASHIER;
}
