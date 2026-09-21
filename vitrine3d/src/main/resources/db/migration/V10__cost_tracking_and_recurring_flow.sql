-- V10: Rastreamento de custo (CMV) e separação de contas fixas no fluxo de caixa.

-- Custo do produto (origem) e snapshot no item vendido (registro de verdade).
-- O snapshot é obrigatório: product_id é referência fraca, então deletar o produto
-- ou editar seu custo não pode reescrever o lucro histórico.
ALTER TABLE products       ADD COLUMN IF NOT EXISTS cost_price numeric(10,2);
ALTER TABLE pdv_sale_items ADD COLUMN IF NOT EXISTS unit_cost  numeric(12,2);

-- Vincula o lançamento de caixa à conta fixa que o originou, permitindo
-- agrupar gastos por conta em vez de casar string de descrição.
ALTER TABLE pdv_cash_flows ADD COLUMN IF NOT EXISTS recurring_expense_id uuid
    REFERENCES recurring_expenses(id);

CREATE INDEX IF NOT EXISTS idx_pdv_sale_items_product ON pdv_sale_items (product_id);
CREATE INDEX IF NOT EXISTS idx_pdv_cashflow_recurring ON pdv_cash_flows (recurring_expense_id);
