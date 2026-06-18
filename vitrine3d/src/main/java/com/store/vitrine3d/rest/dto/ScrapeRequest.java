package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScrapeRequest {
    @NotBlank(message = "URL is required")
    private String url;
}
