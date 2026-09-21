
# Vitrin — Mapa do Sistema

## 1. Visão geral

O Vitrin é uma plataforma para criação de vitrines digitais independentes.

Cada vendedor possui sua própria loja e seus próprios dados.

O Vitrin não é um marketplace:

- lojas não competem dentro de uma mesma vitrine;
- vendedores não acessam dados de outros vendedores;
- cada vendedor administra exclusivamente sua própria loja;
- clientes entram em contato diretamente com o vendedor, principalmente pelo WhatsApp.

---

## 2. Domínios principais

### Auth

Responsável por:

- cadastro;
- login;
- logout;
- verificação de email;
- autenticação Google;
- expiração/revogação de sessão;
- controle de acesso.

Relacionamentos:

Auth → Store
Auth → Admin
Auth → PVD

---

### Store

Representa a loja do vendedor.

Informações principais:

- nome;
- descrição;
- cidade;
- estado;
- telefone;
- slug;
- business type;
- configurações visuais;
- tema/layout;
- informações de apresentação.

Cada usuário possui acesso somente à sua própria Store.

---

### Storefront

É a página pública da loja.

Responsável por:

- apresentar a loja;
- apresentar informações sobre o vendedor;
- exibir produtos;
- exibir categorias/tipos quando aplicável;
- exibir produtos em destaque;
- permitir navegação pelos produtos;
- direcionar o cliente para contato/WhatsApp.

A storefront não deve expor categorias, produtos ou informações pertencentes a outras lojas.

---

### Product

Produto cadastrado pelo vendedor.

Pode possuir:

- nome;
- descrição;
- valor opcional;
- imagens;
- categoria;
- atributos;
- valores dos atributos;
- destaque;
- posição personalizada.

Os produtos são pertencentes a uma Store.

---

### Category

Categoria criada pelo próprio vendedor.

Categorias não são globais.

Cada loja mantém suas próprias categorias.

---

### Attributes

Atributos representam características utilizadas para descrever e filtrar produtos.

Exemplo:

Automóveis:

- Marca
- Ano
- Quilometragem
- Combustível

Uma loja pode possuir atributos predefinidos pelo Business Type e também criar atributos próprios.

Os valores dos atributos podem ser:

- reutilizáveis pela loja;
- específicos de determinado produto.

---

### Business Type

Define o tipo de comércio da loja.

Exemplos:

- roupas;
- móveis;
- automóveis;
- arte;
- alimentos.

O Business Type pode determinar atributos inicialmente sugeridos para a loja.

---

### Admin

Área privada do vendedor.

Permite administrar:

- produtos;
- categorias;
- atributos;
- informações da loja;
- destaques;
- ordenação dos produtos;
- configurações.

O Admin representa o usuário autenticado administrando sua própria Store.

---

### PVD

Módulo premium destinado ao gerenciamento operacional do negócio.

Inclui potencialmente:

- produtos/serviços;
- estoque;
- clientes;
- contas;
- caixa;
- custos;
- receitas;
- relatórios;
- analytics.

O PVD é uma feature separada da storefront, mas utiliza entidades e conceitos do Vitrin.

---

## 3. Relação simplificada

User
  │
  └── Store
       │
       ├── Products
       │    ├── Categories
       │    ├── Attributes
       │    └── Images
       │
       ├── Business Type
       │
       ├── Storefront
       │
       └── PVD
            ├── Customers
            ├── Sales
            ├── Expenses
            ├── Cash Flow
            ├── Inventory
            └── Analytics

---

## 4. Regra fundamental de isolamento

Toda operação administrativa deve estar vinculada ao usuário autenticado.

Um usuário:

- pode visualizar seus próprios dados;
- pode criar seus próprios dados;
- pode alterar seus próprios dados;
- pode excluir seus próprios dados.

Um usuário nunca deve conseguir acessar ou modificar dados de outra Store simplesmente alterando um ID na URL ou request.
