package com.store.vitrine3d.infrastructure.storage;

import java.io.IOException;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import com.store.vitrine3d.domain.service.MakerWorldScraperService;
import com.store.vitrine3d.rest.dto.MakerWorldScrapedDataDTO;

@Service
public class MakerWorldScraperServiceImpl implements MakerWorldScraperService {

    @Override
    public MakerWorldScrapedDataDTO scrape(String url) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10_000)
                    .get();

            String title       = ogMeta(doc, "og:title");
            String description = ogMeta(doc, "og:description");
            String imageUrl    = ogMeta(doc, "og:image");

            return new MakerWorldScrapedDataDTO(title, description, imageUrl);

        } catch (IOException e) {
            throw new RuntimeException("Falha ao fazer scraping da URL: " + url, e);
        }
    }

    private String ogMeta(Document doc, String property) {
        return doc.select("meta[property=" + property + "]").attr("content");
    }
}
