package com.store.vitrine3d.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MakerWorldScrapedDataDTO {
    private String title;
    private String description;
    private String imageUrl;
}
