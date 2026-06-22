package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ScrapeRequest {

    @NotBlank(message = "A URL é obrigatória")
    @Pattern(
        regexp = "^https://(?:www\\.)?makerworld\\.com/.+",
        message = "Apenas URLs do MakerWorld são aceitas (https://makerworld.com/...)"
    )
    private String url;
}
