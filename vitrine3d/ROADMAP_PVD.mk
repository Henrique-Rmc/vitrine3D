Parte 1 — Offline-first (o ponto mais crítico)
Por que isso é o ponto principal?
Tudo o que for registrado offline precisa de uma estratégia antes de qualquer tela ser desenhada.

Tecnologia escolhida: Dexie.js + Background Sync
Dexie.js é uma biblioteca que simplifica o IndexedDB (banco de dados local do navegador, permanente mesmo fechando a aba). Diferente do localStorage, suporta gigabytes, queries e transações.


Fluxo de uma venda offline:
1. Usuário registra venda sem internet
2. Venda é salva no IndexedDB com status: "PENDING_SYNC" + UUID gerado no cliente
3. Quando internet volta → dispara sync automático
4. Backend recebe o batch, processa, marca como "SYNCED"
5. UUID evita duplicatas (servidor ignora se já processou)
Endpoint de sync no backend

POST /api/pdv/sync
Body: {
  sales:      [ ...vendas com clientUUID e timestamp do cliente ],
  cashFlows:  [ ...lançamentos de caixa ],
  stockAdjustments: [ ... ]
}
Response: { processed: 12, skipped: 2, errors: [] }
O servidor usa o clientUUID como idempotency key — se receber o mesmo UUID duas vezes (ex.: sync duplicado), ignora silenciosamente.

Parte 2 — Entidades do backend
Estoque (simples, no próprio produto)

// Adicionado em Product.java
@Column(nullable = false)
private Integer stockQuantity = 0;

@Column(nullable = false)
private Boolean trackStock = false;  // loja decide se controla estoque
Venda (sales)

@Entity @Table(name = "pdv_sales")
Sale {
    UUID id;                          // gerado no servidor
    String clientUuid;                // gerado no cliente — idempotency key
    Store store;
    Customer customer;                // nullable (venda sem cadastro)
    PaymentMethod paymentMethod;      // CASH, PIX, CARD, CREDIT (promissória)
    BigDecimal totalAmount;
    BigDecimal amountPaid;
    BigDecimal changeAmount;
    Instant saleDate;                 // timestamp do cliente (quando ocorreu offline)
    Instant syncedAt;                 // quando chegou ao servidor
    SaleStatus status;                // COMPLETED, CANCELLED
    List<SaleItem> items;
}

SaleItem {
    Long productId;         // nullable (produto pode ter sido deletado depois)
    String productName;     // snapshot no momento da venda
    BigDecimal unitPrice;
    Integer quantity;
    BigDecimal subtotal;
}
Fluxo de caixa (pdv_cash_flows)

CashFlow {
    UUID id;
    String clientUuid;
    Store store;
    FlowType type;           // IN (entrada) ou OUT (saída)
    FlowCategory category;   // SALE, EXPENSE, WITHDRAWAL, CREDIT_PAYMENT...
    BigDecimal amount;
    String description;
    Instant flowDate;
    Instant syncedAt;
}
Clientes (pdv_customers)

Customer {
    UUID id;
    Store store;
    String name;
    String phone;
    String cpf;              // opcional, para controle interno
    String address;
    Instant createdAt;
    List<CustomerCredit> credits;
}

CustomerCredit {
    UUID id;
    Customer customer;
    Sale originSale;         // venda que gerou a dívida (nullable)
    BigDecimal totalDue;
    BigDecimal amountPaid;   // atualizado a cada pagamento parcial
    Instant dueDate;
    CreditStatus status;     // OPEN, PARTIAL, PAID, OVERDUE
    List<CreditPayment> payments;
}

CreditPayment {
    UUID id;
    CustomerCredit credit;
    BigDecimal amount;
    PaymentMethod method;
    Instant paidAt;
}
Parte 3 — Impressora térmica
Não precisa de driver especial. A abordagem mais simples e que funciona em 99% dos casos:


Impressora térmica configurada como impressora no Windows/Linux
→ Frontend chama window.print() 
→ CSS @media print define o layout para papel de 58mm ou 80mm
→ Impressora imprime como qualquer documento
O cupom não fiscal é gerado como HTML puro com CSS específico:


@media print {
  @page { 
    width: 80mm;          /* ou 58mm dependendo da impressora */
    margin: 4mm;
  }
  body { font-family: monospace; font-size: 9pt; }
  /* ... */
}
Quando usar WebUSB/ESC-POS (mais complexo): apenas se precisar de controle fino como gaveta de dinheiro, cortador automático, ou impressoras sem driver de Windows. Deixamos para uma fase posterior.

Estrutura do cupom:


======== COMPROVANTE NÃO FISCAL ========
Loja: Vitrine 3D - ARTE
Data: 31/07/2025  14:32

PRODUTO              QTD    TOTAL
Chaveiro Dragão       2    R$ 30,00
Suporte de Celular    1    R$ 45,00
----------------------------------------
TOTAL               R$ 75,00
Pago (PIX)          R$ 75,00
Troco               R$  0,00

Obrigado pela preferência!
========================================
Parte 4 — Feature gate (Premium)
No backend

// Nova anotação ou check no service
public enum SubscriptionPlan { FREE, BASIC, PRO, PREMIUM }
Antes de qualquer endpoint PDV, o service verifica:


void assertPdvAccess(UUID storeId) {
    Subscription sub = subscriptionRepository.findByStoreId(storeId)...;
    if (sub.getPlan() != SubscriptionPlan.PREMIUM) {
        throw new ForbiddenException("PDV_NOT_AVAILABLE", 
            "PDV requer plano Premium");
    }
}
O admin pode habilitar manualmente via o endpoint já existente:
PUT /api/admin/stores/{id}/subscription — muda o plano para PREMIUM.

Fases de implementação
Fase 1 — Backend (3–4 dias)
 PREMIUM no enum SubscriptionPlan
 Entidades: Sale, SaleItem, CashFlow, Customer, CustomerCredit, CreditPayment
 Enums: PaymentMethod, SaleStatus, FlowType, FlowCategory, CreditStatus
 Repositórios + serviços
 stockQuantity + trackStock em Product
 Endpoints PDV (com gate premium)
 POST /api/pdv/sync com idempotency por clientUuid
Fase 2 — Frontend PDV, tela de venda (4–5 dias)
 Instalar Dexie.js
 Schema local IndexedDB: pendingSales, pendingCashFlows
 Tela de venda: buscar produtos, adicionar ao carrinho, fechar venda
 Lógica offline: salvar no IndexedDB quando sem internet
 Sync automático com window.addEventListener('online', syncPending)
Fase 3 — Fluxo de caixa e estoque (2–3 dias)
 Tela de caixa: entradas/saídas manuais, resumo do dia
 Tela de estoque: quantidade atual, ajuste manual
 Desconto automático no estoque ao fechar venda (trackStock = true)
Fase 4 — Clientes e fiado (3–4 dias) 
 CRUD de clientes
 Tela de crédito: registrar dívida, pagamentos parciais, histórico
 Alerta de clientes com vencimento próximo
Fase 5 — Impressão (1–2 dias)
 Template HTML do cupom (@media print + 80mm)
 Botão "Imprimir cupom" na tela de confirmação de venda
 Teste com impressora física
Fase 6 — Admin e billing (2 dias)
 PREMIUM visível no painel admin
 Toggle manual pelo admin
 Placeholder de "adquira o plano" na UI para lojas sem premium
Estimativa total
Fase	Esforço
Backend	~4 dias
Frontend PDV (venda + offline)	~5 dias
Caixa + Estoque	~3 dias
Clientes + Fiado	~4 dias
Impressão	~2 dias
Gate premium + admin	~2 dias
Total	~20 dias
Por onde começar
O backend das entidades deve ser feito primeiro — sem ele o frontend não tem contrato de API. A ordem natural é:

Enums + entidades + migrations
POST /api/pdv/sync (o endpoint mais crítico — valida a estratégia offline)
Demais endpoints
Frontend
Quer começar pela Fase 1 agora?