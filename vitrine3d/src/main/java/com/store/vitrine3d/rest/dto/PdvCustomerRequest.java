package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PdvCustomerRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    private String phone;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 14)
    private String cpf;

    @Size(max = 200)
    private String address;
}
