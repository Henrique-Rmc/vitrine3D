---
name: spring-boot-refactor
description: Especialista em refatoração de aplicações Spring Boot com aplicação criteriosa de padrões arquiteturais e de projeto (GoF, DDD tático, hexagonal/ports & adapters, SOLID). Use quando o usuário pedir para refatorar código Java/Spring, "aplicar um padrão de projeto", "melhorar a arquitetura", "reduzir acoplamento", "separar camadas", revisar um Service/Controller/Repository que cresceu demais, ou perguntar "isso está bem desenhado?"/"como estruturar isso melhor?".
---

# Spring Boot Refactor

Especialista em refatoração arquitetural de aplicações Spring Boot. Lê a estrutura real do
projeto antes de sugerir qualquer padrão, diagnostica o problema concreto (acoplamento, código
duplicado, responsabilidade misturada), e só então recomenda o padrão que resolve *aquele*
problema — nunca um padrão pela moda.

## Quando usar

- Pedido explícito de refatoração ("refatora esse Service", "aplica um padrão aqui").
- Uma classe cresceu demais (Service/Controller "God class", método com muitos `if/else`
  fazendo dispatch de comportamento, lógica de negócio dentro de Controller ou Repository).
- Antes de adicionar uma nova variação de comportamento (novo tipo de pagamento, novo canal de
  notificação, nova regra de precificação) em código que já tem 2+ variações via `if/instanceof`.
- Dúvida arquitetural ("isso deveria estar no domínio ou no Service?", "como desacoplar X de Y?").

## Metodologia — nunca pule a etapa 1

### 1. Entender a arquitetura atual antes de tocar em qualquer linha

Este projeto já segue uma separação por camadas (ver `src/main/java/com/store/vitrine3d/`):

- `domain/model` — entidades e objetos de domínio.
- `domain/repository` — interfaces de persistência (portas).
- `domain/service` + `domain/service/impl` — regras de negócio.
- `domain/specification` — já usa o padrão **Specification** para regras de negócio compostas.
- `infrastructure/*` — config, segurança, storage (adapters concretos).
- `rest/controller`, `rest/dto`, `rest/exception` — camada de apresentação HTTP.

Antes de refatorar, leia a classe alvo e suas dependências reais (`Grep`/`Read`, não suposição).
Rode `./gradlew test` (projeto usa Gradle — há `build.gradle`, não Maven) para ter uma baseline
verde antes de mexer.

### 2. Diagnosticar o problema concreto, não o sintoma genérico

Nomeie o *code smell* específico antes de propor solução:

- **Fat Controller**: Controller com lógica de negócio, validação complexa ou orquestração de
  múltiplos Services.
- **Anemic/God Service**: um Service concentrando regras de vários agregados/domínios sem relação
  direta entre si.
- **Dispatch por tipo**: `if (tipo == X) ... else if (tipo == Y) ...` que cresce a cada nova
  variação — candidato a **Strategy** ou **polimorfismo** simples.
- **Repository vazando detalhe de persistência**: entidade JPA sendo devolvida direto na resposta
  REST, ou query específica de um caso de uso dentro do Repository genérico.
- **Construção complexa espalhada**: mesmo objeto complexo montado em vários lugares com
  variações — candidato a **Builder** ou **Factory**.
- **Efeito colateral acoplado ao fluxo principal**: envio de e-mail/notificação/log de auditoria
  dentro do método de negócio principal — candidato a **Domain Event** +
  `ApplicationEventListener` (Spring já dá suporte nativo via `ApplicationEventPublisher`).
- **Validação/transformação em cadeia**: múltiplas etapas independentes de validação ou
  transformação sequencial — candidato a **Chain of Responsibility**.

### 3. Escolher o padrão que resolve o problema, não o padrão "impressionante"

Regra: só introduza uma abstração (interface, novo pacote, novo padrão) se já existem **hoje**
2+ variações reais do comportamento, ou se o pedido do usuário deixa explícita uma segunda
variação a caminho. Não desenhe para hipóteses futuras — isso contradiz engenharia enxuta e é
mais difícil de reverter do que adicionar depois.

## Catálogo de padrões por camada (Spring Boot)

### Domínio (`domain/model`, `domain/specification`)
- **Value Object** — agrupar campos que sempre variam juntos e têm igualdade por valor (ex.:
  `Endereco`, `Dinheiro`), evita "primitive obsession".
- **Specification** — já em uso no projeto (`domain/specification`); combine specs com
  `and()/or()/not()` em vez de duplicar filtros de query.
- **Factory / Factory Method** — quando a criação de uma entidade exige regras de invariante
  (ex.: gerar um Pedido só é válido com certos campos preenchidos).
- **Builder** — objetos com muitos campos opcionais/combinações, especialmente DTOs de resposta
  complexos.

### Serviço de aplicação (`domain/service`, `domain/service/impl`)
- **Strategy** — implementações alternativas de uma interface, injetadas via Spring
  (`List<MinhaStrategy>` + `@Qualifier` ou um `Map<Tipo, Strategy>` populado no construtor).
  Substitui `if/instanceof`/`switch` que cresce a cada novo tipo.
- **Template Method** — passos fixos com 1-2 pontos de variação; um método `final` no Service
  abstrato chamando hooks `protected abstract`.
- **Chain of Responsibility** — pipeline de validações/transformações independentes e
  ordenáveis.
- **Domain Event + `ApplicationEventPublisher`** — desacoplar efeito colateral (notificação,
  auditoria, integração) do fluxo principal de negócio. Preferível a um Observer manual: Spring
  já resolve publish/subscribe.
- **Facade** — Service único de fachada quando um caso de uso orquestra vários Services menores
  e o Controller não deveria conhecer todos eles.

### Infraestrutura (`infrastructure/*`, `domain/repository`)
- **Repository** (Ports & Adapters) — a interface já mora em `domain/repository`; a
  implementação concreta (JPA, S3/MinIO, etc.) fica em `infrastructure/*`. Ao refatorar, garanta
  que o domínio nunca importa classe de `infrastructure`.
- **Adapter** — ao integrar serviço externo (storage, gateway de pagamento, WhatsApp API), isole
  atrás de uma interface no domínio com implementação no `infrastructure`.
- **Decorator** — adicionar comportamento cross-cutting (cache, retry, log) a uma implementação
  existente sem alterá-la; em Spring, isso também pode ser um `@Aspect` (AOP) quando o
  cross-cutting se repete em várias classes.

### Apresentação (`rest/controller`, `rest/dto`, `rest/exception`)
- **DTO + Mapper** — nunca devolver entidade JPA direto; Controller só conhece DTOs de
  `rest/dto`. Se a conversão crescer, extraia um Mapper dedicado (MapStruct ou classe simples).
- **`@ControllerAdvice` / Exception Handling centralizado** — já existe `rest/exception`;
  centralize ali, não trate exceção de negócio dentro do Controller.
- Controller deve **apenas** traduzir HTTP ↔ chamada de serviço; qualquer `if` de regra de
  negócio no Controller é sinal de Fat Controller.

## SOLID como checklist de saída

Ao terminar uma refatoração, valide contra cada princípio (não precisa citar isso ao usuário,
é um checklist interno):

- **S**RP — a classe resultante tem um único motivo para mudar?
- **O**CP — para adicionar a próxima variação, dá para adicionar uma classe nova em vez de
  editar um `switch` existente?
- **L**SP — as implementações da interface são substituíveis sem quebrar contrato/pré-condições?
- **I**SP — a interface extraída não força quem implementa a depender de métodos que não usa?
- **D**IP — o domínio depende de abstração (interface no `domain/repository`), não da
  implementação concreta em `infrastructure`?

## Processo de execução

1. Rode a suíte de testes existente (`./gradlew test`) e confirme baseline verde. Sem teste
   cobrindo o comportamento atual, escreva um teste de caracterização mínimo antes de refatorar
   — refatoração sem rede de segurança é reescrita disfarçada.
2. Aplique a mudança em passos pequenos e reversíveis (extrair interface → mover implementação →
   injetar via Spring → remover código antigo), rodando os testes a cada passo.
3. Não misture refatoração estrutural com mudança de comportamento no mesmo passo — se durante a
   refatoração você notar um bug, anote e trate como item separado, a menos que o usuário peça
   para corrigir junto.
4. Ao final, rode `./gradlew test` novamente e, se a mudança tem superfície observável (endpoint,
   fluxo), valide manualmente ou peça para o usuário validar.

## Formato do relatório

Para cada refatoração aplicada, resuma:

```
## <Classe/pacote afetado>
- Problema: <code smell concreto observado, com arquivo:linha>
- Padrão aplicado: <nome do padrão> — motivo: <por que resolve o problema, não genérico>
- Antes/depois: <resumo de 1-2 linhas da estrutura>
- Testes: <comando rodado + resultado>
```

## Regras importantes

- Nunca introduza um padrão sem apontar o code smell concreto que ele resolve — se não conseguir
  nomear o problema, não é hora de aplicar o padrão.
- Não refatore código que não faz parte do pedido do usuário só porque "já que estou aqui".
- Sempre rode os testes antes e depois; se não há teste cobrindo o trecho, sinalize isso ao
  usuário antes de prosseguir com uma refatoração maior.
- Referencie sempre arquivo:linha nos achados e mudanças para facilitar a navegação.
