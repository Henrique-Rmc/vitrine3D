-- Foto de capa individual por loja (stores.cover_image_url) + ate 3 fotos informativas
-- (ex.: "Promoção de 50% em camisas"), guardadas em store_promo_images — mesmo padrao ja
-- usado em product_images (uma linha por foto, ordem preservada em image_order).
--
-- Necessario porque producao roda com spring.jpa.hibernate.ddl-auto=validate (nunca cria
-- schema sozinho) — ver application-prod.properties.
--
-- Idempotente: seguro rodar mais de uma vez (IF NOT EXISTS em tudo).
--
-- QUANDO RODAR: antes do deploy do codigo novo — a entidade Store ja mapeia essas colunas
-- assim que a nova classe subir, e Hibernate valida no boot (ddl-auto=validate) que existem.

BEGIN;

-- 1. Foto de capa — mesma forma de logo_url (coluna simples, uma imagem so).
ALTER TABLE stores ADD COLUMN IF NOT EXISTS cover_image_url varchar(255);

-- 2. Fotos informativas — uma linha por foto, ordem preservada em image_order.
CREATE TABLE IF NOT EXISTS store_promo_images (
    store_id    uuid NOT NULL REFERENCES stores(id),
    image_url   varchar(255) NOT NULL,
    image_order integer NOT NULL,
    PRIMARY KEY (store_id, image_order)
);

-- Verificacao antes de COMMIT
SELECT column_name FROM information_schema.columns
 WHERE table_name = 'stores' AND column_name = 'cover_image_url';
SELECT table_name FROM information_schema.tables WHERE table_name = 'store_promo_images';

-- Se as duas consultas acima retornarem a coluna/tabela esperadas, rode:
COMMIT;
-- Caso contrário, rode ROLLBACK; e investigue antes de tentar de novo.
