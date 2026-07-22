package com.store.vitrine3d.domain.specification;

import com.store.vitrine3d.domain.model.Product;
import jakarta.persistence.criteria.Expression;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaExpression;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    public static Specification<Product> hasProductType(Long productTypeId) {
        return (root, query, cb) -> cb.equal(root.get("productType").get("id"), productTypeId);
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

    // --- Atributos dinamicos (Product.attributes JSONB) ---
    // jsonb_extract_path_text e uma funcao nativa do PostgreSQL: estes predicados so
    // funcionam contra Postgres (nao contra H2, usado nos testes — ver
    // ProductAttributesJsonRepositoryTest para a justificativa e a validacao manual feita
    // contra o Postgres local).

    public static Specification<Product> attributeEquals(String key, String value) {
        return (root, query, cb) -> cb.equal(extractText(cb, root.get("attributes"), key), value);
    }

    public static Specification<Product> attributeNumberBetween(String key, BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            Expression<BigDecimal> value = extractNumber(cb, root.get("attributes"), key);
            if (min != null && max != null) return cb.between(value, min, max);
            if (min != null) return cb.greaterThanOrEqualTo(value, min);
            return cb.lessThanOrEqualTo(value, max);
        };
    }

    /** Usado pra checar se algum produto ainda usa uma chave antes de apagar seu AttributeDefinition. */
    public static Specification<Product> hasAttributeKey(String key) {
        return (root, query, cb) -> {
            HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
            return cb.isTrue(hcb.function("jsonb_exists", Boolean.class, root.get("attributes"), hcb.literal(key)));
        };
    }

    public static Specification<Product> attributeDateBetween(String key, LocalDate min, LocalDate max) {
        return (root, query, cb) -> {
            Expression<LocalDate> value = extractDate(cb, root.get("attributes"), key);
            if (min != null && max != null) return cb.between(value, min, max);
            if (min != null) return cb.greaterThanOrEqualTo(value, min);
            return cb.lessThanOrEqualTo(value, max);
        };
    }

    private static Expression<String> extractText(jakarta.persistence.criteria.CriteriaBuilder cb,
                                                    Expression<?> attributesColumn, String key) {
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
        return hcb.function("jsonb_extract_path_text", String.class, attributesColumn, hcb.literal(key));
    }

    private static Expression<BigDecimal> extractNumber(jakarta.persistence.criteria.CriteriaBuilder cb,
                                                          Expression<?> attributesColumn, String key) {
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
        JpaExpression<String> text = hcb.function("jsonb_extract_path_text", String.class, attributesColumn, hcb.literal(key));
        return hcb.cast(text, BigDecimal.class);
    }

    private static Expression<LocalDate> extractDate(jakarta.persistence.criteria.CriteriaBuilder cb,
                                                       Expression<?> attributesColumn, String key) {
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
        JpaExpression<String> text = hcb.function("jsonb_extract_path_text", String.class, attributesColumn, hcb.literal(key));
        return hcb.cast(text, LocalDate.class);
    }
}
