package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StoreRegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String password;

    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 100, message = "Username must be between 2 and 100 characters")
    private String userName;

    @NotBlank(message = "Store name is required")
    @Size(min = 2, max = 100, message = "Store name must be between 2 and 100 characters")
    private String storeName;

    @NotBlank(message = "WhatsApp number is required")
    @Pattern(regexp = "\\d{10,15}", message = "WhatsApp number must contain 10 to 15 digits only")
    private String whatsappNumber;

    @Size(max = 500, message = "Store description must not exceed 500 characters")
    private String storeDescription;

    private Long stateId;
    private Long cityId;
}
