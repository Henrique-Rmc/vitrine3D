package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.rest.dto.MakerWorldScrapedDataDTO;

public interface MakerWorldScraperService {
    MakerWorldScrapedDataDTO scrape(String url);
}
