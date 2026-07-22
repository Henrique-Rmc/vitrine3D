-- Product.productType deixa de ser opcional: todo produto precisa ter um ProductType.
-- Produtos ja existentes (sem tipo) recebem automaticamente um ProductType "Geral" criado
-- pra loja deles, preservando os dados — depois disso a coluna vira NOT NULL de verdade.
--
-- Necessario porque producao roda com spring.jpa.hibernate.ddl-auto=validate (nunca cria
-- schema sozinho) — ver application-prod.properties. Depende do schema de
-- 2026-07-14-product-types.sql ja existir (tabela product_types, coluna products.product_type_id).
--
-- Idempotente: seguro rodar mais de uma vez (INSERT ... WHERE NOT EXISTS, UPDATE so linhas
-- ainda nulas, SET NOT NULL so aplica se ainda nao estiver aplicado — Postgres nao reclama
-- se a coluna ja for NOT NULL).
--
-- QUANDO RODAR: antes do deploy do codigo novo (o Product.productType do jar novo mapeia
-- a coluna como NOT NULL).

BEGIN;

-- 1. Cria um ProductType "Geral" pra cada loja que tem produto sem tipo ainda.
INSERT INTO product_types (store_id, product_type_key, label, sort_order)
SELECT DISTINCT p.store_id, 'geral', 'Geral', 0
FROM products p
WHERE p.product_type_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM product_types pt
      WHERE pt.store_id = p.store_id AND pt.product_type_key = 'geral'
  );

-- 2. Aponta os produtos sem tipo pro "Geral" da propria loja.
UPDATE products p
SET product_type_id = pt.id
FROM product_types pt
WHERE p.product_type_id IS NULL
  AND pt.store_id = p.store_id
  AND pt.product_type_key = 'geral';

-- 3. Agora que todo produto tem tipo, a coluna vira obrigatoria.
ALTER TABLE products ALTER COLUMN product_type_id SET NOT NULL;

-- Verificacao antes de COMMIT — deve vir 0.
SELECT count(*) AS produtos_ainda_sem_tipo FROM products WHERE product_type_id IS NULL;

-- Se a consulta acima veio 0, rode:
COMMIT;
-- Caso contrário, rode ROLLBACK; e investigue antes de tentar de novo.
