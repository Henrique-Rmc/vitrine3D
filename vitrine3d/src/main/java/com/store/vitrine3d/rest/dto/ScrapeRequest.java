package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScrapeRequest {
    @NotBlank
    private String url;
}
