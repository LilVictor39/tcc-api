# Texto para atualizar o documento da Entrega 4

Texto proposto para adaptar ao documento do TCC. Ao copiar para o Word, preserve o estilo e a numeração das seções. Atualize as passagens da Entrega 3 que ainda tratam validação, HTTPS, limitação de chamadas e tratamento de erros como pendências. Mantenha como pendentes a implantação em nuvem e a avaliação comparativa final. Identifique separadamente os resultados da preparação com H2 e a execução local descrita no documento fornecido pelo autor.

## Entrega 4 — Validação de entradas e proteção das requisições

A API de tarefas foi ampliada com validação de entradas no servidor, HTTPS, limitação de requisições e tratamento seguro de erros. Foram preservados o cadastro e login com JWT e a autorização por proprietário implementados na Entrega 3.

A validação dos DTOs rejeita títulos vazios ou acima de 255 caracteres, descrições acima de 1.000 caracteres e status fora do conjunto PENDENTE, EM_ANDAMENTO e CONCLUIDA. Os IDs de tarefa devem ser inteiros positivos. E-mails têm formato validado e limite de 254 caracteres. As senhas têm entre 8 e 72 caracteres e não podem ultrapassar 72 bytes em UTF-8, inclusive no login. Campos extras são rejeitados; o cliente não pode atribuir proprietário, ID ou data de criação. O corpo da requisição é limitado a 16.384 bytes antes de sua interpretação pelo Jackson. Essas decisões seguem a orientação de definir as entradas aceitas e limitar seu tamanho no servidor ([OWASP FOUNDATION, s.d., Input Validation Cheat Sheet](REFERENCIAS_E_CITACOES.md#r8)).

O servidor foi configurado para aceitar HTTPS na porta 8443, com TLS 1.2 e TLS 1.3. O certificado autoassinado é gerado localmente para `localhost` e validado pelos clientes de teste por confiança explícita, sem desativar a verificação do certificado. Esse certificado atende ao laboratório local e não representa implantação pública nem certificação para produção. O uso de HTTPS se apoia na [OWASP REST Security Cheat Sheet](REFERENCIAS_E_CITACOES.md#r7).

O limitador utiliza janela fixa de 60 segundos por IP da conexão, com até 120 requisições gerais e até 10 requisições às rotas de autenticação dentro da mesma janela. O esgotamento retorna status 429, corpo JSON e cabeçalho `Retry-After`. O estado é mantido em memória, com capacidade máxima de 10.000 IPs e remoção de entradas vencidas. A [RFC 6585](REFERENCIAS_E_CITACOES.md#r9) fundamenta o status 429 e a possibilidade de indicar o tempo de espera (NOTTINGHAM; FIELDING, 2012, seção 4). O mecanismo foi projetado para uma instância de laboratório: reinícios limpam a contagem, réplicas não compartilham cotas e vários usuários atrás de um mesmo NAT podem compartilhar a cota. Não se conclui proteção completa contra negação de serviço.

Os erros tratados pela aplicação têm os campos `timestamp`, `status`, `error`, `message` e `fields`. As respostas não apresentam stack trace, SQL, valores rejeitados, senhas ou tokens. Foram acrescentados handlers para erros de validação, JSON inválido, identificação de recurso, autenticação, limitação e falhas internas. Erros de protocolo rejeitados antes da aplicação podem ser respondidos pelo contêiner e estão fora desse formato JSON.

## Validação da preparação e execução local

No ambiente de preparação foram aprovados 48 testes automatizados: 17 de regressão da Entrega 3, 19 de validação e erros, oito do limitador e quatro de HTTPS. Os testes de HTTPS utilizaram servidor Tomcat real, confiança no certificado e validação de hostname. Uma verificação adicional por Python, também em HTTPS, aprovou 67 asserções; esse total inclui verificações de formato e não representa 67 cenários independentes. Os testes com persistência utilizaram H2 em modo PostgreSQL.

O documento de instruções da Entrega 4 fornecido pelo autor registra uma segunda execução em 27/10/2026: 48 testes aprovados no WSL, com H2 nas suítes, e 67 asserções do verificador HTTPS contra a API local conectada ao PostgreSQL. As capturas mostram o resumo do Maven, a inicialização do Tomcat em HTTPS na porta 8443, a rejeição de título vazio com `400` e uma resposta `429` com `Retry-After: 53` após tentativas repetidas de login. Esse valor é o tempo restante da janela naquele instante, não a duração configurada da janela.

O CSV registra apenas os resultados das asserções; não contém data, identificador do banco nem dados que comprovem sozinho o ambiente de execução. Relacione-o às capturas e à descrição do ambiente. Os 67 resultados da preparação e os 67 da execução local não são 134 cenários independentes.

Ao selecionar as evidências para o texto final, preserve a distinção entre as Entregas 3 e 4:

1. Entrega 3: autenticação, resposta `401` sem token e bloqueio de `GET`, `PUT` e `DELETE` da tarefa de outra conta com `404`.
2. Entrega 4: execução em HTTPS, 48 testes no WSL, validação com `400` e limitação com `429` e `Retry-After`.
3. Preparação: resultados com H2, identificados como ambiente distinto do PostgreSQL local.

Oculte tokens e senhas nas capturas. As verificações manuais no ZAP não substituem uma varredura ativa completa da versão protegida.

A revisão bibliográfica foi organizada em nove fontes e seis tópicos com citações diretas. Os registros completos estão em [REFERENCIAS_E_CITACOES.md](REFERENCIAS_E_CITACOES.md). As fontes de fuzzing são trabalhos relacionados e não comprovam execução de fuzzing automatizado nesta API.

A implementação de registro estruturado de eventos, implantação em nuvem, nova análise de segurança e comparação de desempenho permanecem nas etapas seguintes. As evidências atuais não demonstram mitigação integral do OWASP API Security Top 10.
