-- Migra Product de uma unica imagem (products.image_url) para multiplas fotos
-- (nova tabela product_images, ordenada, ate 5 por produto).
--
-- Necessario porque producao roda com spring.jpa.hibernate.ddl-auto=validate (nunca
-- cria schema sozinho) — ver application-prod.properties.
--
-- QUANDO RODAR: depois do deploy do codigo novo (o Hibernate so vai gravar/ler
-- product_images depois que a entidade Product.imageUrls estiver no jar em producao).
--
-- Idempotente: seguro rodar mais de uma vez (IF NOT EXISTS / ON CONFLICT / IF EXISTS).

BEGIN;

-- 1. Nova tabela de fotos — uma linha por foto, ordem preservada em image_order
CREATE TABLE IF NOT EXISTS product_images (
    product_id  bigint NOT NULL REFERENCES products(id),
    image_url   varchar(255) NOT NULL,
    image_order integer NOT NULL,
    PRIMARY KEY (product_id, image_order)
);

-- 2. Migra a imagem unica existente de cada produto pra primeira posicao (0)
INSERT INTO product_images (product_id, image_url, image_order)
SELECT id, image_url, 0
FROM products
WHERE image_url IS NOT NULL
ON CONFLICT (product_id, image_order) DO NOTHING;

-- 3. Remove a coluna antiga — o dado ja foi migrado no passo 2
ALTER TABLE products DROP COLUMN IF EXISTS image_url;

-- Verificacao antes de COMMIT
SELECT count(*) AS produtos_com_foto_migrada FROM product_images;
SELECT column_name FROM information_schema.columns
 WHERE table_name = 'products' AND column_name = 'image_url';

-- Se a segunda consulta vier vazia (coluna removida) e a primeira bater com o numero
-- esperado de produtos que tinham imagem antes da migracao, rode:
COMMIT;
-- Caso contrario, rode ROLLBACK; e investigue antes de tentar de novo.
