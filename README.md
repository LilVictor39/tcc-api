# tcc-api — Baseline (Entrega 1)

API REST basica em Spring Boot + PostgreSQL, **sem mecanismos de seguranca**,
usada como ponto de partida do TCC1 ("Segurança em Nuvem — Mecanismos de
segurança para APIs REST").

## Pré-requisitos

- Java 17+ (`java -version`)
- Maven (ou usar o wrapper `./mvnw`, se preferir gerar um)
- Docker + Docker Compose

## Como rodar

1. Subir o PostgreSQL:

   ```bash
   docker compose up -d
   ```

2. Rodar a aplicação:

   ```bash
   mvn spring-boot:run
   ```

   A API sobe em `http://localhost:8080`.

3. Testar os endpoints (exemplos com curl):

   ```bash
   # Criar tarefa
   curl -X POST http://localhost:8080/api/tarefas \
     -H "Content-Type: application/json" \
     -d '{"titulo":"Configurar ambiente","descricao":"Spring Boot + PostgreSQL","usuarioId":1}'

   # Listar tarefas
   curl http://localhost:8080/api/tarefas

   # Buscar por id
   curl http://localhost:8080/api/tarefas/1

   # Atualizar
   curl -X PUT http://localhost:8080/api/tarefas/1 \
     -H "Content-Type: application/json" \
     -d '{"titulo":"Configurar ambiente (ok)","descricao":"Feito","status":"CONCLUIDA","usuarioId":1}'

   # Excluir
   curl -X DELETE http://localhost:8080/api/tarefas/1
   ```

## Estrutura

```
src/main/java/br/edu/ceulp/tccapi/
├── TccApiApplication.java     # classe principal
├── model/Tarefa.java          # entidade de dominio
├── repository/                # acesso a dados (Spring Data JPA)
└── controller/                # endpoints REST (CRUD sem seguranca)
```

## Importante — por que "sem segurança" é proposital

Este é o **baseline** da Entrega 1/2. O objetivo do TCC é medir o *antes x
depois* da adoção de mecanismos de segurança. Por isso, nesta fase:

- Não há autenticação nem autorização.
- Qualquer requisição pode ler/alterar/excluir qualquer tarefa, inclusive
  de outro `usuarioId`.
- Isso será usado na Entrega 2 para rodar o OWASP ZAP e documentar as
  vulnerabilidades iniciais (ex: ausência de autenticação, possível BOLA —
  OWASP API1:2023 — já que não há checagem de dono do recurso).

O Spring Security ainda não foi adicionado — isso é objetivo da Entrega 3.

## Próximos passos (próximas entregas)

- **Entrega 2 (27/09):** rodar OWASP ZAP contra esta API e documentar o
  baseline de vulnerabilidades.
- **Entrega 3 (11/10):** adicionar Spring Security + JWT + controle de
  acesso por objeto (dono da tarefa).
- **Entrega 4 (25/10):** validação de entrada (Bean Validation já está no
  `pom.xml`), HTTPS, rate limiting (ex: Bucket4j), tratamento de erros.
- **Entrega 5 (08/11):** reavaliação com OWASP ZAP e comparação com o
  baseline.
- **Entrega 6 (22/11):** deploy em nuvem (AWS/Azure/GCP) + consolidação
  comparativa.
