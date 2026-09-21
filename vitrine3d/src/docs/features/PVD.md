# Vitrin PVD

# Roadmap

## PVD MVP

- [X] Login PVD
- [ ] Feature Gate
- [ ] Produtos/serviços
- [ ] Clientes
- [ ] Vendas
- [ ] Caixa diário
- [ ] Despesas
- [ ] Relatório básico

## PVD V2

- [ ] Estoque
- [ ] Estoque mínimo
- [ ] Alertas
- [ ] Insumos
- [ ] Contas a receber
- [ ] Contas a pagar

## PVD V3

- [ ] Analytics avançado
- [ ] Clientes mais lucrativos
- [ ] Previsões
- [ ] Fornecedores
- [ ] Compras
- [ ] Automação de reposição

## 1. Objetivo

O PVD é o módulo de gerenciamento operacional do negócio.

Ele será uma feature premium do Vitrin.

O objetivo é permitir que o vendedor acompanhe:

- vendas;
- clientes;
- estoque;
- caixa;
- custos;
- receitas;
- lucro;
- indicadores;
- relatórios.

---

# 2. Feature Gate

O PVD somente pode ser utilizado quando:

Store.subscription/premium
        ↓
PVD enabled
        ↓
Acesso permitido

Usuários sem PVD:

- conseguem visualizar que o PVD existe;
- visualizam o recurso como bloqueado;
- não conseguem acessar suas funcionalidades.

---

# 3. Login

O PVD terá uma tela de login própria.

Após autenticação:

Login PVD
   ↓
Área PVD

---

# 4. Produtos e serviços

O PVD deve utilizar a estrutura de produtos existente no Vitrin quando possível.

Deve permitir cadastrar:

- produtos;
- serviços;
- preço;
- custo;
- quantidade;
- categoria;
- atributos.

---

# 5. Clientes

Cada cliente possui um perfil.

Dados:

- nome;
- CPF;
- email;
- telefone;
- histórico de compras;
- pagamentos;
- pendências;
- saldo em aberto.

---

# 6. Vendas

-Uma venda deve permitir adicionar um valor para um produto ja cadastrado na hora do pagamento pois existem

produtos que não tem valor definido inicialmente

Uma venda deve registrar:

- cliente;
- produtos/serviços;
- quantidade;
- valor unitário;
- desconto;
- valor total;
- forma de pagamento;
- status;
- data.

---

# 7. Caixa

O sistema deve permitir:

### Entradas

- vendas;
- recebimentos;
- outros recebimentos.

### Saídas

- despesas;
- compras;
- pagamentos;
- outros custos.

### Fechamento diário

Registrar:

- saldo inicial;
- entradas;
- saídas;
- saldo esperado;
- saldo informado;
- diferença.

---

# 8. Despesas

Permitir cadastrar:

- nome;
- categoria;
- valor;
- periodicidade;
- data;
- observação.

Exemplos:

- aluguel;
- energia;
- internet;
- material;
- higiene;
- manutenção.

---

# 9. Estoque

O estoque deve ser opcional.

Uma loja pode possuir PVD sem utilizar controle de estoque.

Entretanto:

- o recurso pertence ao pacote premium;
- somente lojas premium podem ativá-lo;
- quando ativado, produtos podem possuir controle de quantidade.

---

# 10. Materiais / insumos

O sistema poderá possuir uma entidade própria para itens utilizados na operação.

Exemplo:

Consultório odontológico:

- luvas;
- álcool;
- máscaras;
- materiais odontológicos;
- produtos de limpeza.

Cada item pode possuir:

- nome;
- categoria;
- quantidade;
- estoque mínimo;
- custo;
- fornecedor;
- unidade de medida.

---

# 11. Alertas

Quando estoque < estoque mínimo:

Gerar alerta:

"Item X está com estoque baixo."

Futuramente:

- rotina diária;
- notificações;
- sugestão de reposição.

---

# 12. Relatórios

## Diário

- vendas;
- entradas;
- saídas;
- caixa;
- saldo.

## Semanal

- faturamento;
- despesas;
- lucro;
- número de vendas;
- clientes.

## Mensal

- faturamento;
- custos;
- lucro;
- margem;
- crescimento;
- ticket médio;
- produtos mais vendidos;
- serviços mais vendidos;
- clientes mais lucrativos.

---

# 13. Clientes

Indicadores:

- cliente que mais compra;
- cliente que mais gera lucro;
- frequência de compra;
- ticket médio;
- inadimplência;
- pagamentos pendentes.

---

# 14. Analytics

Indicadores iniciais:

Faturamento
Lucro
Despesas
Ticket médio
Quantidade de vendas
Clientes ativos
Produtos mais vendidos
Serviços mais vendidos

---

# 15. Evolução futura

Possíveis extensões:

- fornecedores;
- compras;
- contas a pagar;
- contas a receber;
- recorrência;
- emissão fiscal;
- notificações;
- previsão de estoque;
- previsão financeira;
- dashboards avançados.
