-- V9: Preço original por item de venda (rastreamento de desconto por item).

ALTER TABLE pdv_sale_items
    ADD COLUMN IF NOT EXISTS original_unit_price numeric(12,2);
