-- V7: Rastreamento de desconto em vendas e vínculo produto/serviço em débitos de clientes.

-- ── pdv_sales: campo de preço original (antes do desconto) ───────────────────
ALTER TABLE pdv_sales
    ADD COLUMN IF NOT EXISTS original_amount numeric(12,2);

-- ── pdv_customer_credits: produto/serviço vinculado e desconto ────────────────
ALTER TABLE pdv_customer_credits
    ADD COLUMN IF NOT EXISTS product_name    varchar(255),
    ADD COLUMN IF NOT EXISTS product_id      bigint,
    ADD COLUMN IF NOT EXISTS original_amount numeric(12,2);

-- Preenche product_name para linhas existentes (garantia para NOT NULL futuro)
UPDATE pdv_customer_credits SET product_name = 'Não especificado' WHERE product_name IS NULL;
