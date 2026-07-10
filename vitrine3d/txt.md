
# Detalhamento da Arquitetura Dinâmica - VitreIn

A arquitetura implementada resolve o desafio de permitir que o sistema se adapte a qualquer tipo de negócio (carros, arte, roupas) sem precisar que um programador altere o banco de dados a cada novo nicho adicionado.

Abaixo, explico cada componente detalhadamente:

## 1. O "Esquema" Dinâmico (BusinessType + AttributeDefinition)

* **O que é:** Em vez de "chumbar" no código que um carro tem "quilometragem" e uma roupa tem "tamanho", o sistema cria um "molde" no banco de dados.
* **Como funciona:**
  * `BusinessType` é o nicho (ex: Automóveis).
  * `AttributeDefinition` são as regras desse nicho (ex: "Quilometragem" é um Número, é Obrigatório, serve para Filtro).
* **O pulo do gato:** A chave `businessType` foi colocada como opcional (FK opcional) nas Lojas (`Store`). Isso significa que se você já tem lojas cadastradas no seu banco de dados atual, o sistema não vai quebrar. As lojas antigas continuam funcionando, e as novas podem adotar o novo formato.

## 2. O Armazenamento (Product.attributes em JSONB)

* **O que é:** Lembra da nossa conversa sobre `JSONB`? Foi exatamente isso que foi aplicado.
* **Como funciona:** Na classe do Produto (`Product`), existe um campo chamado `attributes` que usa a anotação `@JdbcTypeCode(SqlTypes.JSON)`. Isso avisa ao Java: *"Pegue esse dicionário de dados e salve como um JSON nativo no banco"*.
* **A vantagem:** Se um lojista salvar um carro, o banco guarda `{"ano": 2020, "marca": "Fiat"}`. Se salvar um quadro, guarda `{"estilo": "Aquarela"}`. Tudo na mesma coluna, de forma extremamente leve e rápida.

## 3. O Cérebro da Operação (Padrão Strategy / AttributeTypeHandler)

* **O que é:** Como o sistema sabe que "2020" é um número (para filtrar carros de 2015 a 2022) e "Aquarela" é um texto? Através do Padrão de Projeto *Strategy*.
* **Como funciona:** Em vez de criar um código gigante cheio de `IF/ELSE` (`if tipo == numero faz isso, if tipo == data faz aquilo`), foi criado um *Handler* (um tratador) para cada tipo de dado:
  * `NumberHandler`: Sabe validar números e criar filtros de "maior que / menor que".
  * `TextHandler`: Sabe validar textos e criar filtros de "contém a palavra".
  * `EnumHandler`: Sabe validar listas fechadas (ex: P, M, G).
* **A vantagem:** O *Registry* do Spring injeta o tratador correto automaticamente. Se no futuro você quiser adicionar um atributo de "Cor" (com paleta hexadecimal), basta criar um `ColorHandler` e o sistema aceita sem quebrar o resto.

## 4. A Barreira de Proteção (ProductAttributeValidator)

* **O que é:** O guarda de trânsito dos dados.
* **Como funciona:** Quando um lojista tenta salvar um produto, este validador olha para o `BusinessType` da loja e verifica: *"O lojista preencheu todos os atributos obrigatórios para este nicho? Ele está tentando inventar um atributo que não existe?"*. Isso garante que o banco de dados não vire um lixo de informações incorretas.

## 5. O Motor de Busca Dinâmica (ProductAttributeFilterBuilder + ProductSpec)

* **O que é:** É o que permite que a barra de pesquisa do seu site funcione.
* **Como funciona:** Usa a tecnologia *JPA Specifications*. Quando o cliente clica no filtro "Carros entre 2015 e 2020", esse construtor monta a consulta SQL (o código de busca no banco) de forma dinâmica. Ele entende as faixas de valores (`_min` e `_max`) e pluga isso na busca que o seu sistema já tinha, sem precisar reescrever a função original de pesquisa.

## 6. A Ponte com o Frontend (BusinessTypeController)

* **O que é:** São as rotas da API (URLs) que o seu frontend (React) vai chamar.
* **Como funciona:** O React faz um `GET /api/business-types/1/attributes` e a API responde: *"Para o negócio ID 1 (Automóveis), você precisa desenhar na tela um campo numérico para Quilometragem e um campo de seleção para Marca"*. É isso que permite que a tela mude sozinha dependendo do lojista.

---

## 🚨 O "Achado Importante" (H2 vs Postgres)

Esta é uma questão técnica de testes automatizados:

* **O Contexto:** Geralmente, programadores usam um banquinho de dados falso e temporário chamado **H2** para rodar testes rápidos no código, enquanto o **PostgreSQL** é usado no mundo real (Produção).
* **O Problema:** O H2 não suporta as funções avançadas de pesquisa dentro de JSON que o Postgres possui (como `jsonb_extract_path_text`).
* **A Decisão:** A busca dinâmica foi escrita usando as funções reais do Postgres (para ser rápido em produção). Por causa disso, o teste automático não consegue rodar no H2.
* **O Próximo Passo:** O desenvolvedor sugeriu usar **Testcontainers**. É uma tecnologia que "sobe" um PostgreSQL real e invisível via Docker apenas durante os testes, para garantir que os filtros estão funcionando. É uma excelente recomendação técnica.

---

## ⚙️ Notas Operacionais Importantes

1. **Migração de Banco de Dados (`ddl-auto`):** Atualmente, o sistema cria as tabelas automaticamente em desenvolvimento (`update`). Porém, em produção (no seu servidor Render), a regra é `validate` (ele não cria tabelas, apenas verifica se elas existem). Isso significa que, antes do lançamento oficial, você precisará criar essas tabelas manualmente no seu banco Neon ou adotar uma ferramenta como o **Flyway** (que controla as versões do banco de dados automaticamente).
2. **Dados Pré-Carregados (Seeds):** Como não existe uma tela administrativa ("Painel do Dono do Site") para você criar os nichos (Automóveis, Arte, etc.), esses dados estão sendo injetados diretamente via código (Seed). Para validar a ideia agora, isso é perfeito. No futuro, você pode pedir a criação de um CRUD Administrativo para gerenciar isso por uma interface visual.

```eof

### Em Resumo

O que você tem em mãos agora é uma base de código **extremamente moderna e escalável**. Essa arquitetura absorve o crescimento do seu modelo de negócio sem que você precise reescrever o sistema a cada novo tipo de cliente que você decidir aceitar na plataforma.

Sobre as Notas Operacionais: você já tem algum script SQL (como arquivos `schema.sql` ou `data.sql`) sendo executado no seu projeto, ou pretende adicionar o **Flyway** agora para que ele crie essas novas tabelas (BusinessTypes, Attributes) automaticamente no seu banco de dados de produção lá no Neon?
```
