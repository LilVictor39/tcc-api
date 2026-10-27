# TCC API — Entrega 3

Autenticação JWT e autorização por proprietário. O baseline original está preservado no pacote, em `baseline-original.zip`. Esta versão é destinada ao experimento local do TCC.

## Iniciar no seu WSL

Requisitos: Java 17, Maven, Docker Compose, OpenSSL e Python 3.

1. Encerre a aplicação antiga com Ctrl+C no terminal que executa `mvn spring-boot:run`.
2. Na pasta antiga, execute `docker compose stop`. Isso libera a porta 5432; não exclui volumes.
3. Extraia este pacote em uma pasta NOVA, por exemplo `~/projetos/tcc-entrega3`.
4. Entre na pasta `tcc-api` do pacote e execute:

```bash
bash iniciar.sh
```

O script cria uma chave aleatória local em `.jwt-secret` com permissão restrita e inicia PostgreSQL e a API. Não publique esse arquivo. Uma chave já existente é reutilizada entre reinícios. Você também pode fornecer `JWT_SECRET` externamente em Base64, com ao menos 32 bytes aleatórios. Não use uma senha memorizável como chave.

Foi mantida a versão Spring Boot 3.3.4 do experimento para controlar mudanças; isso não representa auditoria de vulnerabilidades das dependências nem recomendação de versão para produção.

## Banco separado do baseline

Esta versão usa banco `tccdb_entrega3`, contêiner `tcc-postgres-entrega3` e volume `tcc_e3_pgdata`. Tarefas antigas NÃO são migradas: `usuarioId` do baseline era um número arbitrário e não pode ser vinculado automaticamente às novas contas. Não execute `docker compose down -v` na pasta antiga. Para voltar ao baseline, pare os serviços da entrega 3 e reinicie os da pasta antiga.

## Executar os testes

Em outro terminal, na pasta desta versão:

```bash
mvn test
python3 verificar_entrega3.py
```

`mvn test` usa H2 isolado com modo PostgreSQL e MockMvc; não exige o PostgreSQL local. O segundo comando envia requisições HTTP para a API real e gera `evidencias_http_entrega3.csv`. Esse teste deve ser executado no seu Windows/WSL com PostgreSQL antes de apresentar a entrega. Ele cria contas fictícias com nomes únicos, que permanecem no banco; remove a tarefa de teste.

## Endpoints

- POST `/api/auth/cadastro`: `{ "email": "a@example.test", "senha": "SenhaTeste123!" }` → 201 e conta sem hash.
- POST `/api/auth/login`: o mesmo formato → 200 com `accessToken`, `tokenType` e `expiresIn`.
- Todas as rotas `/api/tarefas` exigem `Authorization: Bearer SEU_TOKEN`.
- POST/PUT de tarefa: `{ "titulo": "Minha tarefa", "descricao": "Teste", "status": "PENDENTE" }`.
- Não envie `usuarioId`, `id` ou `criadaEm`: o servidor rejeita campos desconhecidos. O dono é definido pelo `sub` do JWT.
- GET lista apenas tarefas do dono. Consulta, alteração e exclusão de recurso alheio retornam 404, assim como recurso inexistente, para reduzir divulgação de existência.
- Ausência de token, token inválido, expirado ou com emissor incorreto → 401.

## Decisões de segurança

Senhas são armazenadas com BCrypt. JWT assinado com HS256, emissor `tcc-api` e validade de 15 minutos, com tolerância de relógio padrão do Spring. A chave fica fora do código. O resource server do Spring Security valida assinatura e expiração; a aplicação restringe o algoritmo a HS256. Os DTOs não permitem escolher ou transferir a propriedade.

CSRF foi desabilitado porque o experimento usa Bearer no cabeçalho e não autenticação automática por cookies; reavaliar essa decisão se o modelo mudar. Tokens não são criptografados. Não contêm senha. Não há refresh token, revogação individual imediata ou logout no servidor: o token é aceito até expirar, e a rotação de chave invalida todos. A chave é exclusiva desta API; não deve ser reutilizada em outros serviços.

O cabeçalho `X-Content-Type-Options: nosniff` passa a ser fornecido pelo Spring Security. Esse efeito deve ser registrado na comparação, pois altera o achado da Entrega 2 junto com a introdução do framework.

## Conferir no ZAP

Mantenha proxy `localhost:8090` e API `localhost:8080`. Use o token de A no Editor Manual de Requisições para criar uma tarefa e copie o ID. Autentique B e repita GET, PUT e DELETE com o mesmo ID: o esperado é 404. Salve requisição e resposta e exporte o relatório/sessão. Não inclua tokens ativos ou senhas nas capturas da versão entregue ao professor.

## Limites desta entrega

HTTPS, rate limiting, auditoria estruturada e tratamento completo de erros pertencem à próxima etapa. A API ainda opera por HTTP local e não deve ser apresentada como pronta para produção ou implantada em nuvem. Não se declara mitigação de todo o OWASP Top 10, nem avaliação de desempenho concluída.

A configuração usa `ddl-auto=update` apenas no laboratório. A implantação futura precisará de migrações e gestão de credenciais próprias. As credenciais do PostgreSQL são fictícias e exclusivas do laboratório.
