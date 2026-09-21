

# Vitrin — Log de Decisões

| Data       | Decisão                                          | Motivo                                       | Impacto                              |
| ---------- | ------------------------------------------------- | -------------------------------------------- | ------------------------------------ |
| 2026-09-07 | Categorias são específicas por Store            | Lojas não devem compartilhar dados          | Category deve possuir Store          |
| 2026-09-07 | Produto possui sortOrder                          | Permitir ordenação manual                  | Product recebe Integer sortOrder     |
| 2026-09-07 | Produto possui Value opcional                     | Alguns vendedores não informam preço       | Campo nullable                       |
| 2026-09-07 | Vitrin não é marketplace                        | Cada vendedor possui sua própria vitrine    | Isolamento obrigatório entre Stores |
| 2026-09-07 | PVD é feature premium                            | Criar modelo de monetização por assinatura | Feature Gate                         |
| 2026-09-07 | Imagens serão processadas antes do armazenamento | Reduzir armazenamento e transferência       | Processamento assíncrono            |


## Regra

Decisões registradas aqui não devem ser apagadas.

Se uma decisão for posteriormente modificada, adicionar uma nova entrada explicando a mudança e referenciando a decisão anterior.
