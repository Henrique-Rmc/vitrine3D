---
name: web-scraping-jsoup
description: Especialista em scraping de páginas externas com JSoup dentro da arquitetura Spring Boot do projeto (ex.: importação de produto via URL do MakerWorld). Use quando o usuário pedir para criar/corrigir um scraper, "importar dados de uma URL", "extrair título/descrição/imagem de um site", adicionar suporte a uma nova origem de scraping, ou revisar um MakerWorldScraperService existente.
---

# Web Scraping com JSoup

Especialista em scraping server-side via JSoup dentro da arquitetura do Vitrine3D. Antes de
escrever qualquer seletor, verifica o estado real do código — a documentação do projeto
(`specifications.txt`, `CONTEXT.md`) descreve um `MakerWorldScraperService` como "completo",
mas isso precisa ser confirmado no código antes de assumir que existe (ver "Armadilha conhecida
deste projeto" abaixo).

## Quando usar

- Pedido para criar um scraper novo ("importar produto de uma URL", "extrair dados do
  MakerWorld/Printables/Thingiverse").
- Corrigir um scraper que passou a extrair dados errados (o site alvo mudou o HTML).
- Adicionar uma segunda origem de scraping a um serviço que hoje só suporta uma.
- Revisar um scraper existente quanto a segurança (SSRF), resiliência e testabilidade.

## Armadilha conhecida deste projeto

`specifications.txt` e `CONTEXT.md` afirmam que existe um `MakerWorldScraperService` /
`MakerWorldScraperServiceImpl`, um `MakerWorldScrapedDataDTO` e o endpoint
`POST /api/products/scrape`, todos "completos". **Confirme com `Glob`/`Grep` antes de assumir
que isso existe** — na última verificação desta skill, nenhum desses arquivos estava presente em
`src/main/java`, nem havia dependência `org.jsoup:jsoup` no `build.gradle`. Se o usuário pedir
para "corrigir" ou "melhorar" o scraper, primeiro confirme se ele de fato existe; se não existir,
trate como implementação nova e avise o usuário da divergência entre documentação e código.

## Onde encaixa na arquitetura

Segue a mesma separação porta/adapter já usada por `StorageService` /
`MinioStorageServiceImpl` (`infrastructure/storage`):

- `domain/service` — interface do scraper (ex.: `MakerWorldScraperService`), sem nenhum import
  de JSoup. Assinatura recebe a URL e devolve um DTO/objeto de domínio já validado.
- `infrastructure/scraping` (ou `infrastructure/storage` se o projeto preferir manter agrupado,
  como o `CONTEXT.md` sugere) — implementação concreta com JSoup, isolada atrás da interface.
- `rest/dto` — DTO de saída (`*ScrapedDataDTO`), nunca devolva o `Document` do JSoup direto pro
  controller.
- `rest/controller` — só traduz `POST /api/products/scrape { url }` → chamada de serviço →
  DTO. Nenhuma lógica de parsing no controller.
- `domain` nunca importa `org.jsoup.*` — só `infrastructure` conhece a biblioteca.

## Metodologia

### 1. Confirmar estado real antes de codar
Rode `Grep`/`Glob` por `Jsoup`, `Scraper`, `scrape` no código e no `build.gradle`. Não confie
apenas na documentação do projeto (ver armadilha acima).

### 2. Inspecionar o HTML alvo antes de escrever seletores
Nunca adivinhe seletores CSS. Peça (ou busque, se autorizado) uma amostra real do HTML da
página alvo, ou rode `Jsoup.connect(url).get()` manualmente para inspecionar a estrutura antes
de codificar `select(...)`. Sites como MakerWorld/Printables/Thingiverse renderizam parte do
conteúdo via JavaScript — confirme que os dados que você precisa (título, descrição, imagem)
estão no HTML estático retornado pelo `GET`, e não injetados por JS client-side; se estiverem
só em JS, JSoup sozinho não resolve (sinalize isso ao usuário em vez de entregar um scraper que
não funciona).

### 3. Segurança: o endpoint recebe URL arbitrária do usuário → risco de SSRF
`POST /api/products/scrape` recebe uma URL informada pelo lojista e o servidor faz a requisição
por ele — isso é um vetor clássico de **SSRF** (Server-Side Request Forgery). Trate como
boundary de segurança, não como scraping "confiável":

- **Allowlist de host**: valide que o host da URL pertence ao domínio esperado (ex.:
  `makerworld.com`) antes de conectar. Rejeite qualquer outro host com erro claro ao usuário.
- **Valide o host final após redirects**: JSoup segue redirect por padrão
  (`.followRedirects(true)`); um domínio permitido pode redirecionar para um IP interno
  (`169.254.169.254`, `localhost`, range `10.0.0.0/8`, etc.). Ou desabilite redirects e valide
  manualmente cada hop, ou resolva o host e rejeite IPs privados/loopback/link-local antes de
  conectar.
- **Timeouts explícitos**: `.timeout(5000)` (ou valor coerente) — nunca deixe o default indefinido
  travar uma request HTTP do backend.
- **Limite de tamanho de resposta**: `.maxBodySize(...)` para não deixar o servidor baixar um
  payload arbitrariamente grande.
- **`User-Agent` identificável**: declare um `User-Agent` próprio (`.userAgent("Vitrine3D/1.0")`),
  não finja ser um navegador — isso é mais transparente e evita ser tratado como bot malicioso.
- Nunca exponha stack trace ou detalhes internos da falha de conexão na resposta HTTP ao
  usuário — traduza para uma mensagem genérica ("não foi possível importar dados dessa URL").

### 4. Resiliência da extração
- Todo `.select(...)`/`.selectFirst(...)` pode devolver vazio — trate `null`/lista vazia
  explicitamente, nunca assuma que o seletor sempre casa (o site alvo muda o HTML sem aviso).
- Tenha um fallback por campo (ex.: se não achar `og:title`, tentar `<h1>`) só se isso já for um
  requisito conhecido — não gold-plate com fallbacks especulativos para casos não pedidos.
- Normalize espaços/quebras de linha do texto extraído (`.text()` do JSoup já ajuda, mas confira
  ` `/espaços duplicados vindos do HTML de origem).
- **Sanitize antes de persistir**: título/descrição extraídos vão para o banco e depois são
  renderizados na vitrine pública — trate como entrada não confiável (mesmo cuidado de um input
  de usuário comum). Não insira HTML bruto extraído da página; extraia texto (`.text()`), não
  `.html()`, a menos que haja sanitização explícita na camada de apresentação.
- Imagem: valide que a URL extraída é absoluta (`element.absUrl("src")`, não `attr("src")`) e
  aponta para um esquema `http(s)` antes de usá-la ou baixá-la.

### 5. Testabilidade
`Jsoup.connect(url)` é uma chamada estática de rede — não mockável diretamente. Isole atrás de
um método pequeno e sobrescrevível (ou um wrapper injetável) para permitir teste sem rede real:

```java
// no service de infraestrutura
protected Document fetch(String url) throws IOException {
    return Jsoup.connect(url).timeout(5000).userAgent("Vitrine3D/1.0").get();
}
```

Nos testes, prefira `Jsoup.parse(htmlFixtureString)` com uma amostra de HTML salva em
`src/test/resources` em vez de bater na rede real — testes de scraping que dependem do site
externo são frágeis (quebram quando o site muda, ficam lentos, falham em CI sem rede).

## Formato do relatório (ao entregar ou revisar um scraper)

```
## <Origem> (ex.: MakerWorld)
- Estado encontrado: <existia / não existia — divergência com specifications.txt?>
- Campos extraídos: <título, descrição, imagem — seletor usado para cada>
- Segurança: <allowlist de host? redirects validados? timeout? maxBodySize?>
- Resiliência: <o que acontece se um seletor não casar?>
- Testes: <fixture de HTML local usada, comando rodado>
```

## Regras importantes

- Nunca escreva um scraper que aceite qualquer host sem allowlist — isso é SSRF, não um detalhe
  de qualidade.
- Nunca devolva `.html()` bruto extraído do site alvo para ser persistido/renderizado sem
  sanitização.
- Não adicione suporte especulativo a múltiplas origens se o pedido é para uma única — extraia
  a interface `domain/service` primeiro, mas só implemente a origem pedida.
- Sempre confirme com `Grep`/`Glob` se a infraestrutura de scraping já existe antes de assumir a
  documentação (`specifications.txt`/`CONTEXT.md`) como verdade — ela pode estar desatualizada.
- Referencie sempre arquivo:linha nos achados e mudanças para facilitar a navegação.
