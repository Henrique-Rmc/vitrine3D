package com.store.vitrine3d.domain.specification;

import com.store.vitrine3d.domain.model.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public final class ProductSpec {

    private ProductSpec() {}

    public static Specification<Product> fromStore(UUID storeId) {
        return (root, query, cb) -> cb.equal(root.get("store").get("id"), storeId);
    }

    public static Specification<Product> isVisible() {
        return (root, query, cb) -> cb.isTrue(root.get("isVisible"));
    }

    public static Specification<Product> storeIsActive() {
        return (root, query, cb) -> cb.isTrue(root.get("store").get("isActive"));
    }

    public static Specification<Product> isFeatured() {
        return (root, query, cb) -> cb.isTrue(root.get("featured"));
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasMaterial(Long materialId) {
        return (root, query, cb) -> cb.equal(root.get("material").get("id"), materialId);
    }

    public static Specification<Product> nameContains(String keyword) {
        String pattern = "%" + keyword.toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_") + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Product> minPrice(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Product> maxPrice(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), max);
    }
}
