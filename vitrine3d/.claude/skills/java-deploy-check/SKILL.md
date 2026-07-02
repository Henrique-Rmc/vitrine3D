---
name: java-deploy-check
description: Checklist especialista para validar se uma aplicação Java (Spring Boot, Maven ou Gradle) está pronta para um deploy limpo e seguro em produção. Use quando o usuário pedir para revisar/preparar/auditar um deploy, perguntar "está pronto para produção?", "checklist de deploy", "posso subir isso em prod?", ou pedir uma checagem geral antes de publicar a aplicação.
---

# Java Deploy Check

Especialista em auditoria pré-deploy de aplicações Java. Percorre build, testes, segredos,
configuração de segurança, banco de dados, imagem Docker e observabilidade, executando
comandos reais no repositório em vez de assumir que algo está correto.

## Quando usar

- Usuário pede para revisar/preparar um deploy ("posso subir isso em prod?", "checklist de deploy").
- Antes de um merge para a branch de produção ou de criar uma release.
- Depois de mudanças em configuração, segurança, Dockerfile ou dependências.

## Como conduzir a checagem

Não pule etapas. Rode comandos de verdade (Bash/PowerShell) para confirmar cada item —
não infira "provavelmente está ok" sem checar. Ajuste os comandos ao gerenciador de build
detectado:

- Se existir `pom.xml` → Maven (`mvnw`/`mvn`).
- Se existir `build.gradle`/`build.gradle.kts` → Gradle (`gradlew`).

Vá categoria por categoria. Para cada item, chegue a um veredito **PASS / WARN / FAIL**
com a evidência (comando rodado, arquivo:linha, ou output). No final, produza um relatório
único agrupado por categoria (veja "Formato do relatório").

### 1. Build e dependências

- Build limpo compila sem erros: `./gradlew clean build -x test` ou `mvn clean package -DskipTests`.
- Nenhum `SNAPSHOT` de dependência própria indo para produção (`grep -rn "SNAPSHOT" build.gradle pom.xml`).
- Dependências desatualizadas/vulneráveis: `./gradlew dependencies` / `mvn versions:display-dependency-updates`,
  e se houver plugin de CVE (OWASP dependency-check, `gradle-versions-plugin`), rodá-lo.
- Nenhum plugin ou dependência `developmentOnly`/`provided` vazando para o jar final
  (ex.: `spring-boot-devtools`, `spring-boot-docker-compose` devem ser `developmentOnly`).

### 2. Testes

- Suite completa passa: `./gradlew test` ou `mvn test`.
- Testes de integração relevantes (segurança, autenticação, endpoints críticos) existem e passam.
- Nenhum teste marcado como `@Disabled`/`@Ignore` escondendo uma regressão conhecida.

### 3. Segredos e configuração

- Nenhum segredo hardcoded no código-fonte ou em `application.properties`/`.yml` versionado:
  `grep -rniE "password|secret|api[_-]?key|token" src/main/resources` e comparar com o que
  está de fato em variável de ambiente (`${VAR:default}`) vs. valor fixo.
- Valores de fallback de segredos (`${JWT_SECRET:algum-valor-fixo}`) NÃO podem ser os valores
  usados em produção — confirmar que o ambiente de deploy sobrescreve todos eles.
- `.env`, `application-prod.properties` ou qualquer arquivo com credenciais reais está no
  `.gitignore` e não foi commitado (`git log --all --oneline -- **/.env` / `git show HEAD:<arquivo>`).
- Existe um profile de produção separado (`application-prod.properties`/`application-prod.yml`)
  com overrides explícitos, e não apenas os defaults de dev.
- Flags de "modo dev" desligadas em prod: `ddl-auto=validate` (nunca `update`/`create`),
  `show-sql=false`, cookies `secure=true`, `spring.jpa.hibernate.ddl-auto` sem fallback perigoso.

### 4. Segurança da API (Spring Security ou equivalente)

- CORS restrito a origens conhecidas (não `*` em produção).
- CSRF: desabilitado apenas se a API for stateless com JWT e isso for intencional — confirmar,
  não assumir.
- Rate limiting em rotas sensíveis (login, registro, reset de senha) contra força bruta.
- Endpoints do Actuator (`/actuator/**`) não expostos publicamente sem autenticação
  (`management.endpoints.web.exposure.include`, `management.endpoint.health.show-details`).
- Validação de entrada (Bean Validation / `@Valid`) nos controllers que recebem payload externo.
- DTOs de resposta não vazam entidades JPA completas (senha hash, campos internos).
- Se há upload de arquivos: validação de MIME type/tamanho no backend, não só no frontend.

### 5. Banco de dados

- Migrações versionadas (Flyway/Liquibase) em vez de `ddl-auto=update`/`create` — se não
  houver ferramenta de migração, sinalizar como risco para schema evolution em produção.
- Índices em colunas de busca/filtro frequentes existem (checar entidades JPA).
- Pool de conexões (HikariCP) com limites explícitos, não os defaults arbitrários.

### 6. Imagem Docker / containerização

- Build multi-stage (build e runtime separados) para não levar JDK/toolchain para produção.
- Imagem final roda como usuário não-root (`USER` no Dockerfile) — se ausente, é FAIL.
- `.dockerignore` cobre `.git`, `build/`, `.gradle/`, arquivos de segredo.
- Nenhuma variável de ambiente sensível com valor real hardcoded no `Dockerfile`/`compose.yaml`
  versionado (valores de exemplo tipo `minioadmin`/senha default são aceitáveis só em compose
  de desenvolvimento local, nunca em manifesto de produção).
- Healthcheck configurado (Docker `HEALTHCHECK` ou Spring Actuator `/actuator/health`).
- Porta exposta e variáveis de runtime documentadas.

### 7. Observabilidade e logs

- Logs não imprimem dados sensíveis (senha, token, JWT, PII) — `grep -rn "log\.\(info\|debug\|error\)"`
  perto de código de autenticação.
- Nível de log em produção não é `DEBUG`/`TRACE` por padrão.
- Health check/liveness endpoint disponível para o orquestrador (load balancer, k8s, etc.).

### 8. Performance e recursos

- Limites de JVM (`-Xmx`, `-Xms`) definidos condizentes com o recurso do host/container,
  não deixados no default.
- Limites de upload/request coerentes com o que o proxy reverso permite.
- Timeouts de conexão com serviços externos (banco, storage) configurados, não infinitos.

## Formato do relatório

Produza uma tabela por categoria, e feche com um veredito geral:

```
## Build e dependências
- [PASS] Build limpo ok (`./gradlew clean build -x test`)
- [WARN] 3 dependências com major version desatualizada (ver lista)
- [FAIL] `spring-boot-devtools` sem escopo `developmentOnly` — vaza para produção

...

## Veredito geral: NÃO PRONTO (2 FAIL, 4 WARN)
```

Todo item **FAIL** bloqueia o deploy até ser corrigido. Itens **WARN** devem ser reportados
ao usuário com a recomendação, mas não bloqueiam por si só — deixe a decisão final de
prosseguir para o usuário.

## Regras importantes

- Nunca marque um item como PASS sem ter rodado o comando ou lido o arquivo que comprova isso.
- Não corrija nada automaticamente sem perguntar — esta skill é de auditoria/diagnóstico.
  Se o usuário pedir para corrigir os problemas encontrados, trate isso como um pedido
  separado e explícito antes de editar arquivos de configuração de produção ou segredos.
- Referencie sempre arquivo:linha nos achados para facilitar a navegação.
