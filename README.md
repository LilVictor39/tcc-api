# TCC API — Entrega 3

API REST de tarefas para o experimento local do TCC. Esta entrega adiciona autenticação com JWT e autorização por proprietário: cada conta acessa apenas as próprias tarefas.

## Requisitos

Use o WSL com Java 17, Maven, Docker Compose e OpenSSL. Python 3 é necessário para a verificação HTTP. As portas locais `8080` (API) e `5432` (PostgreSQL) precisam estar livres.

Se outra versão da API estiver rodando, encerre-a com Ctrl+C. Se ela também usar a porta `5432`, execute `docker compose stop` na pasta dessa versão; o comando preserva os volumes.

## Iniciar a aplicação

No WSL, entre na pasta deste projeto e execute:

```bash
cd /caminho/para/tcc-api
bash iniciar.sh
```

O script gera uma chave aleatória local em `.jwt-secret` usando `umask 077`, inicia o PostgreSQL e executa `mvn spring-boot:run`. A API fica em `http://localhost:8080`. Não publique `.jwt-secret`; uma chave já existente é reutilizada entre reinícios. Você também pode fornecer `JWT_SECRET` externamente em Base64, com ao menos 32 bytes aleatórios após a decodificação. Não use uma senha memorizável como chave.

Para parar a API, pressione Ctrl+C no terminal do Maven. Para parar também o banco, execute `docker compose stop` nesta pasta.

Foi mantida a versão Spring Boot 3.3.4 do experimento para controlar mudanças; isso não representa auditoria de vulnerabilidades das dependências nem recomendação de versão para produção.

## Banco separado do baseline

Esta versão usa o banco `tccdb_entrega3`, o contêiner `tcc-postgres-entrega3` e o volume lógico `tcc_e3_pgdata` do Docker Compose. Tarefas antigas não são migradas: `usuarioId` do baseline era um número arbitrário e não pode ser vinculado automaticamente às novas contas. Evite `docker compose down -v` se quiser preservar os dados de qualquer uma das versões. Para voltar ao baseline, pare os serviços da entrega 3 e reinicie os da pasta antiga.

## Executar os testes

Em outro terminal, na pasta desta versão:

```bash
mvn test
python3 verificar_entrega3.py
```

`mvn test` usa H2 isolado em modo PostgreSQL e MockMvc; não exige o banco local. `verificar_entrega3.py` exige a API e o PostgreSQL em execução, envia requisições HTTP e gera `evidencias_http_entrega3.csv`. Execute essa verificação no ambiente local antes da apresentação. Ela cria duas contas fictícias com nomes únicos, que permanecem no banco, e remove a tarefa de teste.

## Endpoints

| Método | Rota | Resultado |
| --- | --- | --- |
| `POST` | `/api/auth/cadastro` | `201` com `id` e `email`; e-mail já cadastrado retorna `409`. |
| `POST` | `/api/auth/login` | `200` com `accessToken`, `tokenType` e `expiresIn`; credenciais inválidas retornam `401`. |
| `GET` | `/api/tarefas` | `200` com as tarefas da conta autenticada. |
| `GET` | `/api/tarefas/{id}` | `200` para uma tarefa própria. |
| `POST` | `/api/tarefas` | `201` com a tarefa criada. |
| `PUT` | `/api/tarefas/{id}` | `200` com a tarefa atualizada. |
| `DELETE` | `/api/tarefas/{id}` | `204` sem corpo. |

Cadastro e login recebem `{ "email": "a@example.test", "senha": "SenhaTeste123!" }`. O cadastro não devolve senha nem hash. Para usar as rotas de tarefas, envie o token do login em `Authorization: Bearer SEU_TOKEN`. Ele expira em 15 minutos (`expiresIn: 900`).

POST e PUT de tarefa recebem `{ "titulo": "Minha tarefa", "descricao": "Teste", "status": "PENDENTE" }`. Os valores aceitos para `status` são `PENDENTE`, `EM_ANDAMENTO` e `CONCLUIDA`. Não envie `usuarioId`, `id` ou `criadaEm`: o servidor rejeita campos desconhecidos. O dono é definido pelo `sub` do JWT.

GET lista apenas tarefas do dono. Consulta, alteração e exclusão de recurso alheio retornam `404`, assim como recurso inexistente, para reduzir a divulgação de sua existência. Token ausente, inválido, expirado ou com emissor incorreto retorna `401`.

Exemplo de cadastro e login no WSL:

```bash
curl -i -X POST http://localhost:8080/api/auth/cadastro -H 'Content-Type: application/json' -d '{"email":"a@example.test","senha":"SenhaTeste123!"}'
curl -i -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{"email":"a@example.test","senha":"SenhaTeste123!"}'
```

Copie `accessToken` da resposta do login para usar nas requisições a `/api/tarefas`.

## Decisões de segurança

Senhas são armazenadas com BCrypt. JWT assinado com HS256, emissor `tcc-api` e validade de 15 minutos, com tolerância de relógio padrão do Spring. A chave fica fora do código. O resource server do Spring Security valida assinatura, expiração e emissor; a aplicação restringe o algoritmo a HS256. Os DTOs não permitem escolher ou transferir a propriedade.

CSRF foi desabilitado porque o experimento usa Bearer no cabeçalho e não autenticação automática por cookies; reavaliar essa decisão se o modelo mudar. Tokens não são criptografados. Não contêm senha. Não há refresh token, revogação individual imediata ou logout no servidor: o token é aceito até expirar, e a rotação de chave invalida todos. A chave é exclusiva desta API; não deve ser reutilizada em outros serviços.

O cabeçalho `X-Content-Type-Options: nosniff` passa a ser fornecido pelo Spring Security. Esse efeito deve ser registrado na comparação, pois altera o achado da Entrega 2 junto com a introdução do framework.

## Conferir no ZAP

Mantenha proxy `localhost:8090` e API `localhost:8080`. Use o token de A no Editor Manual de Requisições para criar uma tarefa e copie o ID. Autentique B e repita GET, PUT e DELETE com o mesmo ID: o esperado é 404. Salve requisição e resposta e exporte o relatório/sessão. Não inclua tokens ativos ou senhas nas capturas da versão entregue ao professor.

## Limites desta entrega

HTTPS, rate limiting, auditoria estruturada e tratamento completo de erros pertencem à próxima etapa. A API ainda opera por HTTP local e não deve ser apresentada como pronta para produção ou implantada em nuvem. Não se declara mitigação de todo o OWASP Top 10, nem avaliação de desempenho concluída.

A configuração usa `ddl-auto=update` apenas no laboratório. A implantação futura precisará de migrações e gestão de credenciais próprias. As credenciais do PostgreSQL são fictícias e exclusivas do laboratório.
