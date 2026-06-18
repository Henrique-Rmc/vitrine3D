package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Data
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String material;
    private Boolean multicolor;
    private String dimensions;
    private Boolean isVisible;
    private Boolean featured;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private UUID storeId;
    private String whatsappUrl;
    private long clickCount;

    public static ProductResponse from(Product product) {
        return from(product, 0L);
    }

    public static ProductResponse from(Product product, long clickCount) {
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setMaterial(product.getMaterial());
        dto.setMulticolor(product.getMulticolor());
        dto.setDimensions(product.getDimensions());
        dto.setIsVisible(product.getIsVisible());
        dto.setFeatured(product.getFeatured());
        dto.setPrice(product.getPrice());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setStoreId(product.getStore().getId());
        dto.setWhatsappUrl(buildWhatsappUrl(product));
        dto.setClickCount(clickCount);
        return dto;
    }

    private static String buildWhatsappUrl(Product product) {
        String number = product.getStore().getWhatsappNumber().replaceAll("[^0-9]", "");
        String text = "Olá, vi o produto *" + product.getName() + "* na sua vitrine e gostaria de mais informações!";
        return "https://wa.me/" + number + "?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
