package com.store.vitrine3d.rest.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.store.vitrine3d.domain.model.LayoutMode;
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

    @Size(max = 150)
    private String addressStreet;

    @Size(max = 20)
    private String addressNumber;

    @Size(max = 100)
    private String addressNeighborhood;

    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido")
    private String addressZipCode;

    private Long stateId;
    private Long cityId;
    private LayoutMode layoutMode;

    @Size(max = 100)
    private String presetSlug;
    private StoreProfileType profileType;

    // PatchField distingue "não enviado" (não mexe no tema) de "enviado como null" (remove a
    // personalização) — ver PatchField para o porquê de String/@Pattern não servirem aqui.
    // Validado manualmente em UserServiceImpl.update(), já que @Pattern não se aplica a este tipo.
    @JsonDeserialize(using = PatchFieldStringDeserializer.class)
    private PatchField<String> storeNameFont = PatchField.absent();

    @JsonDeserialize(using = PatchFieldStringDeserializer.class)
    private PatchField<String> coverColor = PatchField.absent();

    @JsonDeserialize(using = PatchFieldStringDeserializer.class)
    private PatchField<String> storeTheme = PatchField.absent();
}
