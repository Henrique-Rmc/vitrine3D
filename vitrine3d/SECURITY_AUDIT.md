## Suas Tarefas Obrigatórias:

1. **Auditoria de Autenticação (JWT):** Validar como os tokens são gerados, assinados, expirados e armazenados (evitando armazenamento vulnerável a XSS no frontend).
2. **Proteção de API (Spring Security):** Configurar regras estritas de CORS, mitigar CSRF (se aplicável), e garantir que as rotas `/api/users/register` e `/api/auth/login` possuam Rate Limiting para evitar ataques de força bruta.
3. **Validação de Inputs:** Garantir que todas as requisições de entrada possuam sanitização e validação rígida no backend (Jakarta Validation) para prevenir SQL Injection e ataques de payload malicioso.
4. **Segurança de Arquivos (MinIO):** Revisar as políticas do bucket. Garantir que apenas usuários autenticados possam fazer upload de imagens e que o tipo de arquivo (`MIME type`) seja estritamente validado no backend antes do envio para o storage.
5. **Vazamento de Dados:** Analisar os objetos de retorno (DTOs) para garantir que entidades completas de banco de dados (com senhas criptografadas ou IDs internos) nunca sejam enviadas ao frontend.
