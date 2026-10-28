# TCC API — evolução das Entregas 1 a 4

API REST de tarefas usada no experimento do TCC sobre segurança de APIs. Este README reúne a evolução do projeto em um só lugar. A branch `entrga4` contém a implementação mais recente: JWT e autorização por proprietário, validação de entradas, HTTPS, limitação de requisições e erros JSON padronizados. As descrições das etapas anteriores são históricas; os comandos de execução abaixo se aplicam à versão atual.

## Evolução do projeto

| Entrega | Objetivo | O que mudou |
| --- | --- | --- |
| 1 — baseline | Criar uma API REST para comparação. | CRUD de tarefas com Spring Boot e PostgreSQL, em HTTP, sem autenticação ou autorização. |
| 2 — avaliação inicial | Examinar o baseline com OWASP ZAP. | Duas operações GET e um tipo de alerta passivo com duas ocorrências; não houve varredura ativa completa documentada. |
| 3 — identidade e propriedade | Restringir o acesso às tarefas. | Cadastro, login, JWT, senhas com BCrypt e consultas filtradas pelo proprietário. |
| 4 — proteção das requisições | Acrescentar controles de entrada e transporte. | Validação, limite do corpo, HTTPS local, limitação por IP e respostas de erro padronizadas. |

### Entrega 1 — baseline

A versão da branch `entrga1` expõe `GET`, `POST`, `PUT` e `DELETE` em `/api/tarefas`, na porta HTTP `8080`. Não usa Spring Security. O cliente informa `usuarioId` ao criar ou alterar uma tarefa, e qualquer requisição pode listar, consultar, alterar ou excluir tarefas de outros usuários. Essa ausência deliberada de controle é o ponto de comparação para as etapas seguintes; não corresponde ao comportamento da branch atual.

### Entrega 2 — análise do baseline

O documento do TCC fornecido pelo autor registra uma análise em 08/10/2026: API em HTTP na porta `8080`, ZAP 2.17.0 no Windows e proxy na porta `8090`. Foram examinadas duas operações `GET`. O ZAP registrou **um tipo de alerta passivo, com duas ocorrências**: `X-Content-Type-Options Header Missing` (regra 10021, risco baixo, confiança média). Não há relatório exportado ou evidência de varredura ativa completa. A ausência de autenticação e de checagem de propriedade é demonstrada pelo código do baseline; o ZAP não demonstrou uma exploração autenticada de BOLA. A introdução de `X-Content-Type-Options: nosniff` pelo Spring Security deve entrar na comparação posterior.

### Entrega 3 — autenticação e autorização

Foram acrescentados cadastro e login, senhas armazenadas com BCrypt e JWT assinado com HS256. O token tem emissor `tcc-api`, validade de 15 minutos e identifica a conta no claim `sub`. A chave aleatória fica fora do código e deve ser exclusiva desta API. O Spring Security valida assinatura, expiração e emissor; a aplicação aceita apenas HS256. As rotas de tarefas exigem `Authorization: Bearer SEU_TOKEN`.

O servidor define `usuarioId` a partir do token; os DTOs não permitem que o cliente escolha ou transfira a propriedade. A listagem retorna apenas tarefas da conta autenticada. Consulta, alteração e exclusão de tarefa alheia retornam `404`, assim como um ID inexistente. O banco `tccdb_entrega3`, o contêiner `tcc-postgres-entrega3` e o volume lógico `tcc_e3_pgdata` do Compose foram separados do baseline. As tarefas antigas não são migradas: o `usuarioId` do baseline era arbitrário e não pode ser associado automaticamente às novas contas.

Os testes da Entrega 3 usam MockMvc e H2 em modo PostgreSQL. O documento do TCC registra **17 testes aprovados no WSL** e **16 verificações HTTP aprovadas contra PostgreSQL** na porta `8081`, usada naquele ambiente porque a `8080` estava ocupada. O verificador histórico está em `verificar_entrega3.py` e gera `evidencias_http_entrega3.csv`. Em teste manual, a conta A criou uma tarefa; a conta B recebeu `404` ao consultar, alterar e excluir o mesmo ID, e A confirmou depois que a tarefa permanecia intacta. Preserve requisições e respostas para comparação e oculte tokens e senhas nas capturas. Esses resultados não representam uma varredura ativa completa.

CSRF foi desabilitado porque o experimento usa Bearer no cabeçalho, sem autenticação automática por cookies. Não há refresh token, logout no servidor ou revogação individual imediata: tokens continuam válidos até expirar, e a rotação da chave invalida todos. Reavalie essas decisões se o modelo de autenticação mudar.

### Entrega 4 — versão atual

A Entrega 4 mantém as rotas e a autorização da Entrega 3 e acrescenta as regras, o HTTPS e as verificações descritas a seguir. O Spring Boot 3.3.4 foi mantido para controlar as mudanças do experimento; isso não equivale a uma auditoria das dependências nem a uma recomendação para produção.

## Executar a versão atual no WSL

Requisitos: Java 17 com `keytool`, Maven, Docker Compose, OpenSSL e Python 3. Execute os comandos na pasta desta branch, que contém `pom.xml`. Antes de iniciar, libere a porta `8443` e, se outra instância do PostgreSQL ocupar a porta `5432`, pare-a na pasta correspondente com `docker compose stop`.

```bash
cd /caminho/para/tcc-api
mvn test
bash iniciar.sh
```

O script inicia o PostgreSQL definido em `docker-compose.yml` e usa o banco `tccdb_entrega3`. O volume existente do **mesmo projeto Docker Compose** é reutilizado; em outra pasta ou com outro nome de projeto, o Compose pode criar um volume distinto. Evite `docker compose down -v` se quiser preservar os dados.

O script cria `.jwt-secret` se não existir e reutiliza a chave nos próximos inícios. Também é possível definir `JWT_SECRET` externamente em Base64, desde que represente ao menos 32 bytes aleatórios; não use uma senha memorizável como chave. `preparar_https.sh` cria `certs/localhost.p12` e a senha local quando ausentes, reutiliza esses arquivos e exporta `certs/localhost.crt`. Esses segredos e o certificado local ficam fora do Git. Contas do banco reutilizado continuam válidas; tokens anteriores só deixam de valer se a chave JWT mudar.

O endereço é **https://localhost:8443**. Não use `http://localhost:8443`. O perfil HTTPS é o padrão e não abre uma segunda porta HTTP. As portas `8080` e `8081` não pertencem a este processo ao usar o comando acima; se responderem, verifique se outra aplicação ainda está aberta. Para encerrar a API, pressione Ctrl+C; para parar também o banco, execute `docker compose stop` nesta pasta.

O certificado autoassinado é para o laboratório local; o navegador pode pedir confiança explícita. O script de verificação e o curl validam o certificado público fornecido, sem desativar a verificação TLS:

```bash
curl --cacert certs/localhost.crt -i https://localhost:8443/api/tarefas
```

Esperado: `401` em JSON, pois falta o token.

## Rotas principais

| Método | Rota | Resultado esperado |
| --- | --- | --- |
| `POST` | `/api/auth/cadastro` | `201` com `id` e `email`; e-mail duplicado retorna `409`. |
| `POST` | `/api/auth/login` | `200` com `accessToken`, `tokenType` e `expiresIn`; credenciais inválidas retornam `401`. |
| `GET` | `/api/tarefas` | `200` com as tarefas da conta autenticada. |
| `GET` | `/api/tarefas/{id}` | `200` para tarefa própria; `404` para tarefa alheia ou inexistente. |
| `POST` | `/api/tarefas` | `201` com a tarefa criada. |
| `PUT` | `/api/tarefas/{id}` | `200` com a tarefa atualizada. |
| `DELETE` | `/api/tarefas/{id}` | `204` sem corpo. |

Cadastro e login recebem JSON com `email` e `senha`. Para criar ou atualizar uma tarefa, envie `titulo`, `descricao` (opcional) e `status` (`PENDENTE`, `EM_ANDAMENTO` ou `CONCLUIDA`). Todas as rotas de tarefas exigem `Authorization: Bearer SEU_TOKEN`; o token expira em 15 minutos. O servidor define o proprietário a partir do JWT. Os detalhes de validação e os códigos de erro estão nas seções abaixo.

Exemplo no WSL, após iniciar a API:

```bash
curl --cacert certs/localhost.crt -i -X POST https://localhost:8443/api/auth/cadastro -H 'Content-Type: application/json' -d '{"email":"a@example.test","senha":"SenhaTeste123!"}'
curl --cacert certs/localhost.crt -i -X POST https://localhost:8443/api/auth/login -H 'Content-Type: application/json' -d '{"email":"a@example.test","senha":"SenhaTeste123!"}'
curl --cacert certs/localhost.crt -i -X POST https://localhost:8443/api/tarefas -H 'Content-Type: application/json' -H 'Authorization: Bearer SEU_TOKEN' -d '{"titulo":"Minha tarefa","descricao":"Teste","status":"PENDENTE"}'
```

Copie `accessToken` da resposta do login no lugar de `SEU_TOKEN`. Se o e-mail do exemplo já estiver cadastrado, use outro e-mail para repetir o cadastro. Não envie `id`, `usuarioId` ou `criadaEm` no corpo: esses campos são definidos pelo servidor. Antes de executar o verificador automatizado, reinicie a API ou aguarde a janela do limitador para não misturar as cotas dos exemplos com as do teste.

## Verificar a Entrega 4 com PostgreSQL

Com a API recém-iniciada, abra outro terminal:

```bash
cd /caminho/para/tcc-api
python3 verificar_entrega4.py https://localhost:8443 certs/localhost.crt
```

O script verifica erros, limites, validação, isolamento entre duas contas e o certificado HTTPS. Gera `evidencias_https_entrega4.csv`, sem gravar senhas ou tokens. Contém 67 asserções, com verificações de status, formato e ausência de detalhes internos; não são 67 cenários independentes. Em uma execução concluída, cria duas contas fictícias que permanecem no banco e exclui a tarefa usada no teste. Execute sobre banco de laboratório.

Ao final ele esgota deliberadamente os limites da API. Antes de repetir ou fazer capturas, pare com Ctrl+C e execute `bash iniciar.sh` novamente, ou aguarde a janela indicada por `Retry-After`. Outras chamadas feitas antes do script consomem a mesma cota por IP e podem alterar seu resultado.

O verificador da Entrega 3 foi preservado para comparação. Para executá-lo via HTTPS, reinicie a API e use:

```bash
SSL_CERT_FILE=certs/localhost.crt python3 verificar_entrega3.py https://localhost:8443
```

## Regras implementadas

| Entrada | Regra |
|---|---|
| Título | Obrigatório, não pode conter apenas espaços, máximo de 255 caracteres |
| Descrição | Opcional, máximo de 1.000 caracteres |
| Status | Obrigatório: PENDENTE, EM_ANDAMENTO ou CONCLUIDA |
| ID de tarefa | Inteiro positivo |
| E-mail | Obrigatório, formato validado, máximo de 254 caracteres |
| Senha | De 8 a 72 caracteres e no máximo 72 bytes UTF-8, inclusive no login |
| Campos extras | Rejeitados; proprietário, ID e data não são aceitos no DTO |
| Corpo HTTP | Máximo de 16.384 bytes, inclusive sem Content-Length |

A validação não remove pontuação de texto livre nem pretende substituir consultas parametrizadas, autorização ou codificação de saída.

## Limitação de chamadas

- Janela fixa de 60 segundos por endereço IP da conexão.
- Até 120 chamadas gerais e, dentro desse total, até 10 chamadas às rotas `/api/auth/` por janela.
- Chamadas rejeitadas por autenticação também contam. Após esgotamento, retorna `429`, JSON e `Retry-After` em segundos. Respostas não são armazenáveis em cache.
- A contagem é sincronizada, com teste de concorrência. Até 10.000 IPs ativos; entradas vencidas são removidas. Ao atingir a capacidade, novos IPs recebem `503` até liberar espaço, sem crescimento ilimitado.
- `X-Forwarded-For` não altera o IP utilizado. Este modo é de conexão direta, sem proxy reverso. Ao implantar atrás de proxy, rever a identificação do cliente e confiar apenas no proxy administrado.
- Estado apenas em memória: reiniciar limpa a contagem; réplicas não compartilham cotas. Janela fixa permite picos na fronteira. NAT/proxy pode reunir vários usuários na mesma cota. Não equivale a proteção completa contra DDoS.

Configuração opcional: `RATE_API`, `RATE_AUTH` e `RATE_WINDOW_SECONDS`. Não altere os valores ao coletar as evidências padrão. As suítes de validação e JWT usam limites altos para testar cada funcionalidade isoladamente; a suíte do limitador usa cotas baixas e relógio controlado.

## Erros

Erros de aplicação têm `timestamp`, `status`, `error`, `message` e `fields`. Mensagens são controladas; não devolvem stack trace, SQL, senha, token, valores rejeitados ou exceções internas. Os handlers cobrem MVC, autenticação JWT, limitação e fallback `/error`. Falhas de protocolo rejeitadas pelo Tomcat antes da aplicação, como HTTP simples numa porta TLS ou cabeçalhos malformados, podem ter uma resposta própria do contêiner.

## Evidências e documento

[`documentacao/RESULTADOS_PREPARACAO.md`](documentacao/RESULTADOS_PREPARACAO.md) registra uma preparação separada: 48 testes e 67 asserções HTTPS aprovados com H2. O documento do TCC fornecido pelo autor também registra **48 testes aprovados no WSL** (suítes com H2), **67 asserções do verificador HTTPS contra a API local com PostgreSQL** e capturas manuais do ZAP com resposta `400` para título vazio e `429` com `Retry-After` após tentativas repetidas de login. O CSV da raiz e o CSV da preparação têm o mesmo conteúdo, mas registram apenas resultados; **essa igualdade não identifica o banco usado**. O ambiente e a data da execução local são contextualizados pelo documento e suas capturas, não pelo CSV isolado. Os dois conjuntos de 67 asserções não devem ser somados como cenários diferentes.

[`documentacao/TEXTO_ENTREGA4.md`](documentacao/TEXTO_ENTREGA4.md) contém texto para inserir no documento e um roteiro de evidências locais. [`documentacao/REFERENCIAS_E_CITACOES.md`](documentacao/REFERENCIAS_E_CITACOES.md) reúne nove fontes e seis tópicos de citação. Preserve a formatação do seu Word ao copiar.

## Limites atuais e próximas etapas

O registro estruturado de eventos de segurança, a implantação em nuvem, uma nova varredura de segurança e a comparação controlada de desempenho continuam nas próximas etapas. O certificado autoassinado é apenas para `localhost`; esta configuração não está pronta para produção. A API não declara mitigação integral do OWASP API Security Top 10.

`spring.jpa.hibernate.ddl-auto=update` e as credenciais do PostgreSQL são escolhas do laboratório. Uma implantação futura exige migrações de banco, gestão própria de credenciais e configuração adequada de TLS. A versão atual utiliza uma instância e estado de limitação em memória, conforme descrito acima.
