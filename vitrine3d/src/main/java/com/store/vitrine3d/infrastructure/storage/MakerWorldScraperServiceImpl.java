package com.store.vitrine3d.infrastructure.storage;

import com.store.vitrine3d.domain.service.MakerWorldScraperService;
import com.store.vitrine3d.rest.dto.MakerWorldScrapedDataDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

@Service
public class MakerWorldScraperServiceImpl implements MakerWorldScraperService {

    private static final Set<String> ALLOWED_HOSTS = Set.of("makerworld.com", "www.makerworld.com");

    @Override
    public MakerWorldScrapedDataDTO scrape(String url) {
        validateUrl(url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10_000)
                    .get();

            return new MakerWorldScrapedDataDTO(
                    ogMeta(doc, "og:title"),
                    ogMeta(doc, "og:description"),
                    ogMeta(doc, "og:image")
            );

        } catch (IOException e) {
            throw new RuntimeException("Falha ao fazer scraping da URL: " + url, e);
        }
    }

    private void validateUrl(String url) {
        try {
            URI uri = new URI(url);

            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("Apenas URLs HTTPS são aceitas");
            }

            String host = uri.getHost();
            if (host == null || !ALLOWED_HOSTS.contains(host.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException(
                        "URL não permitida. Apenas links do MakerWorld (makerworld.com) são aceitos");
            }

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL inválida");
        }
    }

    private String ogMeta(Document doc, String property) {
        return doc.select("meta[property=" + property + "]").attr("content");
    }
}
