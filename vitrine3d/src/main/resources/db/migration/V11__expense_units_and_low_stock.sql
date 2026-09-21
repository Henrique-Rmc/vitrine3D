-- V11: Unidade de medida e alerta de estoque baixo por material de consumo.
--
-- O estoque passa a ser decimal porque 500 g de um material medido em KG é 0.5 — impossível
-- de representar com integer. O texto livre em "unit" (ex.: "caixa", "pacote") é preservado
-- como rótulo; quem rege o cálculo é a nova coluna stock_unit.

-- low_stock_alert é nulável de propósito: null = alerta não configurado (nunca dispara),
-- zero = "avise quando zerar". Com NOT NULL DEFAULT 0 todo material novo nasceria em alerta.
ALTER TABLE expense_products
    ADD COLUMN IF NOT EXISTS stock_unit      varchar(10)   not null default 'UN',
    ADD COLUMN IF NOT EXISTS low_stock_alert numeric(14,3);

ALTER TABLE expense_products
    ALTER COLUMN low_stock_alert DROP NOT NULL,
    ALTER COLUMN low_stock_alert DROP DEFAULT;

ALTER TABLE expense_products
    ALTER COLUMN stock_quantity TYPE numeric(14,3),
    ALTER COLUMN stock_quantity SET DEFAULT 0;

ALTER TABLE expense_batch_items
    ADD COLUMN IF NOT EXISTS unit_code varchar(10) not null default 'UN';

ALTER TABLE expense_batch_items
    ALTER COLUMN quantity TYPE numeric(14,3),
    ALTER COLUMN quantity SET DEFAULT 1;

-- Deriva a unidade de medida do texto livre que já existia. Qualquer coisa que não seja
-- massa ou volume (ex.: "caixa", "pacote") vira contagem, que é o comportamento atual.
UPDATE expense_products SET stock_unit = CASE
        WHEN lower(btrim(unit)) IN ('kg', '1kg', 'quilo', 'quilos', 'kilo')   THEN 'KG'
        WHEN lower(btrim(unit)) IN ('g', 'grama', 'gramas')                   THEN 'G'
        WHEN lower(btrim(unit)) IN ('l', 'lt', 'litro', 'litros')             THEN 'L'
        WHEN lower(btrim(unit)) IN ('ml', 'mililitro', 'mililitros')          THEN 'ML'
        ELSE 'UN'
    END;

UPDATE expense_batch_items SET unit_code = CASE
        WHEN lower(btrim(unit)) IN ('kg', '1kg', 'quilo', 'quilos', 'kilo')   THEN 'KG'
        WHEN lower(btrim(unit)) IN ('g', 'grama', 'gramas')                   THEN 'G'
        WHEN lower(btrim(unit)) IN ('l', 'lt', 'litro', 'litros')             THEN 'L'
        WHEN lower(btrim(unit)) IN ('ml', 'mililitro', 'mililitros')          THEN 'ML'
        ELSE 'UN'
    END;

-- Materiais que já existiam ficam sem alerta até o lojista definir o próprio limite.
-- O índice parcial ignora naturalmente as linhas com low_stock_alert nulo.
CREATE INDEX IF NOT EXISTS idx_expense_products_low_stock
    ON expense_products (store_id) WHERE stock_quantity <= low_stock_alert;
