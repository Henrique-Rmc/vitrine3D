package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    /** Conveniencia: primeira foto da lista, ou null se o produto ainda nao tem nenhuma. */
    private String imageUrl;
    private List<String> imageUrls;
    private Boolean isVisible;
    private Boolean featured;
    private BigDecimal price;
    private UUID storeId;
    private String whatsappUrl;
    private long clickCount;
    private Map<String, Object> attributes;
    private Long productTypeId;
    private String productTypeLabel;

    public static ProductResponse from(Product product) {
        return from(product, 0L);
    }

    public static ProductResponse from(Product product, long clickCount) {
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setImageUrls(product.getImageUrls());
        dto.setImageUrl(product.getImageUrls().isEmpty() ? null : product.getImageUrls().get(0));
        dto.setIsVisible(product.getIsVisible());
        dto.setFeatured(product.getFeatured());
        dto.setPrice(product.getPrice());
        dto.setStoreId(product.getStore().getId());
        dto.setWhatsappUrl(buildWhatsappUrl(product));
        dto.setClickCount(clickCount);
        dto.setAttributes(product.getAttributes());
        if (product.getProductType() != null) {
            dto.setProductTypeId(product.getProductType().getId());
            dto.setProductTypeLabel(product.getProductType().getLabel());
        }
        return dto;
    }

    private static String buildWhatsappUrl(Product product) {
        String number = product.getStore().getWhatsappNumber().replaceAll("[^0-9]", "");
        String text = "Olá, vi o produto *" + product.getName() + "* na sua vitrine e gostaria de mais informações!";
        return "https://wa.me/" + number + "?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
