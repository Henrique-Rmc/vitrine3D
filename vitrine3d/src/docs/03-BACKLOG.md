
# Vitrin — Backlog

## 🔐 Autenticação

- [ ] Verificação de email no cadastro
- [ ] Login com Google
- [X] Manter usuário autenticado após cadastro
- [ ] Impedir usuário autenticado de acessar login/cadastro
- [X] Redirecionar logout para home
- [X] Criar fluxo de expiração de sessão
- [X] Criar vendedor especial através de URL privada
- [X] Rejeitar links de afiliados que não utilizem HTTPS

---

## 🏪 Loja / Storefront

- [X] Remover Header "Vitrin"
- [X] Adicionar foto de capa
- [ ] Criar seção de apresentação da loja
- [ ] Criar layouts diferentes por tipo de comércio
- [ ] Melhorar filtros da storefront
- [ ] Permitir cidade + estado
- [ ] Ajustar frase de apresentação da loja
- [ ] Remover botão "Ver minha vitrine" quando admin estiver na própria vitrine
- [ ] Substituir por "Meus produtos"
- [ ] Remover "My Dashboard"

---

## 📦 Produtos

- [ ] Corrigir ordenação dos produtos
- [ ] Permitir ordenação manual
- [ ] Persistir sortOrder
- [X] Paginação de produtos
- [X] Limitar listagem a 15 produtos
- [X] Adicionar valor opcional
- [X] Permitir até 5 imagens
- [X] Melhorar upload de imagens
- [X] Exibir todas as imagens do produto
- [X] Corrigir posicionamento das setas das imagens
- [X] Processar imagens antes do armazenamento
- [X] Busca por digitação
- [X] Criar produtos em destaque
- [X] Permitir editar destaque
- [X] Aumentar limite de destaques para 5
- [X] Exibir produtos em ordem de cadastro
  Categorias

## 🗂 Categorias

- [ ] Remover categorias globais
- [ ] Tornar categoria específica da Store
- [ ] Permitir criar categoria durante cadastro de produto
- [ ] Permitir editar nome da categoria
- [ ] Permitir excluir categoria
- [ ] Corrigir atualização da interface após criação
- [ ] Adicionar filtro de categorias
- [ ] Exibir somente categorias que possuem produtos
- [ ] Adicionar explicação sobre categorias
  Atributos

## 🏷 Atributos

- [ ] Remover Multicolor
- [ ] Renomear "Meus Materiais" para "Meus atributos"
- [ ] Remover "Meus tipos"
- [ ] Criar tela explicando atributos
- [ ] Permitir ocultar atributos predefinidos
- [ ] Permitir criar atributos
- [ ] Permitir editar atributos
- [ ] Criar valores reutilizáveis dos atributos
- [ ] Permitir valores específicos por produto
- [ ] Criar filtros dinâmicos
- [ ] Adicionar Specification Pattern
- [ ] Avaliar Builder Pattern
  Business Type

## 🏢 Business Type

- [ ] Criar seleção de Business Type durante cadastro
- [ ] Adicionar opção "Outro"
- [ ] Permitir selecionar até 5 atributos quando escolher "Outro"
- [ ] Expandir seed de Business Types
- [ ] Criar atributos predefinidos por Business Type
  Admin

## 🛠 Admin

- [ ] Corrigir página genérica /admin/products
- [ ] Garantir que produtos sejam filtrados pela Store do usuário
- [ ] Exibir mensagem quando não houver produtos
- [ ] Criar botão para primeiro cadastro
- [ ] Organizar tipos de produto do mais recente para o mais antigo
- [ ] Ajustar comportamento responsivo
- [ ] Fazer excedentes quebrarem para segunda linha no desktop
- [ ] Sincronizar funções desktop/mobile
  Analytics

## 📊 Analytics

- [ ] Contador de visualizações
- [ ] Contador de cliques no WhatsApp
- [ ] Persistir métricas no banco
- [ ] Definir modelo de analytics
  Infraestrutura

## ⚙️ Infraestrutura

- [ ] Criar health check no GitHub
- [ ] Verificar lifecycle do bucket
- [ ] Definir armazenamento seguro de secrets
- [ ] Configurar produção
