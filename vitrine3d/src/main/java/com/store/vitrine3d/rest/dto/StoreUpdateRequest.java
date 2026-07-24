package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.StoreProfileType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StoreUpdateRequest {

    @Size(min = 2, max = 100, message = "Username must be between 2 and 100 characters")
    private String userName;

    @Size(min = 2, max = 100, message = "Store name must be between 2 and 100 characters")
    private String storeName;

    @Pattern(regexp = "\\d{10,15}", message = "WhatsApp number must contain 10 to 15 digits only")
    private String whatsappNumber;

    @Size(max = 500, message = "Store description must not exceed 500 characters")
    private String storeDescription;

    private Long stateId;
    private Long cityId;
    private Long businessTypeId;
    private StoreProfileType profileType;
}
