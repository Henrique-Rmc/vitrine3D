-- Remove os campos fixos de Product que foram substituidos pelo novo modelo de
-- atributos dinamicos (BusinessType/AttributeDefinition/Product.attributes):
-- Category, Material e dimensions.
--
-- CONTEXTO: Category e Material eram usados exclusivamente por Product; dimensions
-- era um campo de texto livre generico (fazia sentido pra objetos impressos em 3D,
-- nao pra automoveis/imoveis/etc.). Como o filtro de produtos passou a ser feito
-- por atributos dinamicos por tipo de negocio, os produtos cadastrados antes dessa
-- mudanca nao tem como ser retrofitados automaticamente (nao sabem a que
-- BusinessType pertencem nem preenchem os atributos exigidos) — por isso a
-- limpeza inclui apagar os produtos existentes, nao so as colunas/tabelas.
--
-- QUANDO RODAR: depois de fazer deploy do codigo que remove Product.category/
-- Product.material/Product.dimensions (caso contrario o Hibernate vai reclamar de
-- colunas que ele ainda espera mapear).
--
-- SEGURANCA: destrutivo e IRREVERSIVEL (apaga todos os produtos e cliques de
-- WhatsApp associados). Faca backup/snapshot do banco antes de rodar em
-- producao. O script roda dentro de uma transacao — confira o resultado das
-- queries de verificacao no final antes de decidir COMMIT ou ROLLBACK.

BEGIN;

-- 1 e 2. Remove produtos e quem referencia products (FK) — precisa ser na mesma
--    instrucao: TRUNCATE valida a constraint estruturalmente, nao por contagem de
--    linhas, entao truncar em statements separados falha mesmo com a tabela
--    dependente ja vazia.
TRUNCATE TABLE whatsapp_clicks, products RESTART IDENTITY;

-- 3. Remove a associacao (agora inexistente no codigo) de categoria/material,
--    e o campo dimensions (idem — substituido por atributo dinamico por vertical)
ALTER TABLE products DROP COLUMN IF EXISTS category_id;
ALTER TABLE products DROP COLUMN IF EXISTS material_id;
ALTER TABLE products DROP COLUMN IF EXISTS dimensions;

-- 4. Category/Material foram removidos inteiramente do codigo — apaga as tabelas
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS materials;

-- Verificacao antes de COMMIT: confira que products esta vazia e sem as colunas,
-- e que categories/materials nao existem mais.
SELECT count(*) AS products_remaining FROM products;
SELECT column_name FROM information_schema.columns
 WHERE table_name = 'products' AND column_name IN ('category_id', 'material_id', 'dimensions');
SELECT table_name FROM information_schema.tables
 WHERE table_name IN ('categories', 'materials');

-- Se os três resultados acima vierem vazios/zero, rode:
COMMIT;
-- Caso contrário, rode ROLLBACK; e investigue antes de tentar de novo.
