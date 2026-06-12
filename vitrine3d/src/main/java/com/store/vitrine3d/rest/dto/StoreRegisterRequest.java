package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StoreRegisterRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    private String password;

    @NotBlank
    private String userName;

    @NotBlank
    private String storeName;

    @NotBlank
    @Pattern(regexp = "\\d{10,15}", message = "Número de WhatsApp inválido (somente dígitos, 10-15)")
    private String whatsappNumber;

    private String storeDescription;
    private Long stateId;
    private Long cityId;
}
