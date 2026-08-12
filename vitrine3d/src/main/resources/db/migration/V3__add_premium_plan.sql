-- V3: Adiciona plano PREMIUM (PDV).
-- As constraints CHECK em plan_limits e subscriptions foram geradas pelo Hibernate com
-- os valores do enum na época (FREE, BASIC, PRO). PREMIUM foi adicionado depois.
-- Recriamos ambas incluindo o novo valor.

ALTER TABLE plan_limits
    DROP CONSTRAINT IF EXISTS plan_limits_plan_check;

ALTER TABLE plan_limits
    ADD CONSTRAINT plan_limits_plan_check
    CHECK (plan IN ('FREE', 'BASIC', 'PRO', 'PREMIUM'));

ALTER TABLE subscriptions
    DROP CONSTRAINT IF EXISTS subscriptions_plan_check;

ALTER TABLE subscriptions
    ADD CONSTRAINT subscriptions_plan_check
    CHECK (plan IN ('FREE', 'BASIC', 'PRO', 'PREMIUM'));

INSERT INTO plan_limits (plan, max_products, max_product_types, max_featured_products, max_promo_images, affiliate_profile_allowed, custom_domain_allowed)
VALUES ('PREMIUM', -1, -1, -1, 10, true, true)
ON CONFLICT (plan) DO NOTHING;
