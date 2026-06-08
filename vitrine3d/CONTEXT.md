# Escopo do Projeto: Vitrine 3D

## Objetivo

Plataforma para profissionais de 3D/Impressão 3D cadastrarem lojas e exibirem produtos em vitrine pública, superando as limitações de organização do Instagram.

## Funcionalidades Principais (MVP)

1. **Cadastro de User/Store:** Lojistas criam suas contas.
2. **Gerenciamento de Categories:** Lojistas criam suas próprias categorias.
3. **Gerenciamento de Products:** Cadastro de produtos associados a uma categoria.
4. **Scraping MakerWorld:** Importar dados/imagens de uma URL do MakerWorld via JSoup.
5. **Vitrine Pública (Mobile/Web):**
   - Home: Exibe produtos mais vendidos/destacados.
   - Header: Menu dinâmico com as categorias da loja.
6. **Fluxo de "Compra":** Botão que redireciona o cliente para o WhatsApp do vendedor com mensagem pré-preenchida contendo os detalhes do produto. (Sem gateway de pagamento.)

## Stack Tecnológica

- Backend: Java com Spring Boot (Spring Data JPA, Spring Web)
- Banco de Dados: PostgreSQL (produção) / H2 (desenvolvimento)
- Scraping: JSoup

## Modelagem de Dados

### User
| Campo             | Tipo    |
|-------------------|---------|
| id                | Long    |
| email             | String  |
| password          | String  |
| userName          | String  |
| storeName         | String  |
| whatsappNumber    | String  |
| storeDescription  | String  |
| logoUrl           | String  |
| createdAt         | Instant |
| updatedAt         | Instant |
| isActive          | Boolean |

### Category
| Campo    | Tipo    |
|----------|---------|
| id       | Long    |
| name     | String  |
| isGlobal | Boolean |
| userId   | Long    |

### Product
| Campo       | Tipo    |
|-------------|---------|
| id          | Long    |
| name        | String  |
| description | String  |
| imageUrl    | String  |
| material    | String  |
| multicolor  | Boolean |
| dimensions  | String  |
| isVisible   | Boolean |
| categoryId  | Long    |
| userId      | Long    |

## Arquitetura de Pacotes

```
com.vitrine3d
├── domain
│   ├── model           # Entidades JPA (User, Category, Product)
│   ├── repository      # Interfaces Spring Data JPA
│   ├── service         # Interfaces de regras de negócio
│   └── service.impl    # Implementações das interfaces de serviço
├── infrastructure
│   └── storage         # Integração com armazenamento (ex: imagens, scraping)
└── rest
    └── controller      # Controllers REST (@RestController)
```

### Regras de Arquitetura

- Toda regra de negócio é definida como **interface** em `domain.service` e implementada em `domain.service.impl`.
- Controllers dependem apenas das interfaces de serviço (nunca das implementações diretamente).
- Repositórios são acessados somente pela camada de serviço.
- A camada `infrastructure.storage` isola integrações externas (JSoup, upload de imagens, etc.).
