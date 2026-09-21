-- V12: Histórico de pagamentos das contas fixas, com comprovantes.
--
-- Até aqui o pagamento de uma conta fixa não virava registro: o payRecurring só avançava
-- lastPaidDate/nextDueDate na definição e gravava uma linha no fluxo de caixa. Sem uma
-- linha por pagamento não há onde anexar comprovante nem onde guardar o valor real do mês
-- (conta de luz varia), e "quanto paguei de aluguel nos últimos 6 meses" fica sem resposta.

CREATE TABLE IF NOT EXISTS recurring_expense_payments (
    id                   uuid          primary key,
    recurring_expense_id uuid          not null references recurring_expenses(id) on delete cascade,
    amount               numeric(12,2) not null,
    paid_at              timestamptz   not null,
    note                 varchar(500),
    -- Sem FK: o lançamento de caixa pode ser estornado sem arrastar o histórico junto.
    cash_flow_id         uuid,
    created_at           timestamptz   not null
);

CREATE INDEX IF NOT EXISTS idx_recurring_payments_expense
    ON recurring_expense_payments (recurring_expense_id, paid_at DESC);

-- Comprovantes (máximo de 2, validado na aplicação) — mesmo padrão de product_images.
CREATE TABLE IF NOT EXISTS recurring_expense_payment_images (
    payment_id  uuid         not null references recurring_expense_payments(id) on delete cascade,
    image_url   varchar(2048) not null,
    image_order integer      not null,
    primary key (payment_id, image_order)
);
