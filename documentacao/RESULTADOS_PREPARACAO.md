# Resultados da preparação da Entrega 4

Execução registrada em 27/10/2026. Ambiente de preparação: Java 17.0.20, Maven 3.9.9 e Spring Boot 3.3.4. Os resultados abaixo pertencem à preparação com H2; não documentam uma execução da API com PostgreSQL no WSL.

| Suíte | Testes | Falhas | Erros | Ignorados |
|---|---:|---:|---:|---:|
| SegurancaIntegrationTest | 17 | 0 | 0 | 0 |
| Entrega4IntegrationTest | 19 | 0 | 0 | 0 |
| RequestLimitsTest | 8 | 0 | 0 | 0 |
| HttpsIntegrationTest | 4 | 0 | 0 | 0 |
| Total | 48 | 0 | 0 | 0 |

O Maven terminou com `BUILD SUCCESS`. Os relatórios textuais do Surefire e o resumo da execução estão em [`evidencias_preparacao/`](evidencias_preparacao/).

- JWT e isolamento por proprietário foram reavaliados nos 17 testes anteriores.
- Validação e respostas de erro foram avaliadas com MockMvc e H2, incluindo uma falha interna provocada apenas em código de teste.
- Limitação foi testada com relógio controlado: cotas gerais e de autenticação, recuperação da janela, separação de IPs, cabeçalho encaminhado falso, memória limitada e concorrência. O teste de concorrência confirma apenas três chamadas aceitas em 30 chamadas para cota três.
- HTTPS foi testado com Tomcat real, porta aleatória, certificado confiado explicitamente pelo cliente e validação de hostname. Foram negociados TLS 1.2 e TLS 1.3; cadastro, login e criação de tarefa passaram via HTTPS. HTTP simples na porta TLS não executou a API e retornou 400 do contêiner.
- O script Python foi executado contra outro processo real da API em HTTPS na porta 8443, usando certificado local e H2. Foram aprovadas 67 asserções, registradas em [`evidencias_preparacao/verificacao_https_H2.csv`](evidencias_preparacao/verificacao_https_H2.csv). Elas incluem verificações secundárias e não correspondem a 67 cenários distintos.

Os testes de persistência nesta preparação usam H2 em modo PostgreSQL. Não havia Docker nem PostgreSQL disponíveis nesse ambiente. O arquivo `evidencias_https_entrega4.csv` na raiz tem o mesmo conteúdo do CSV da preparação com H2; ambos registram apenas cenários e resultados esperados e observados. **O CSV isolado não identifica o banco ou a data da execução**; a igualdade entre arquivos não permite concluir que a reprodução com PostgreSQL deixou de ocorrer.

## Execução local registrada pelo autor

O documento de instruções da Entrega 4 fornecido pelo autor registra, em 27/10/2026, uma execução no WSL com 48 testes aprovados nas suítes com H2 e uma verificação HTTPS de 67 asserções contra a API local conectada ao PostgreSQL. Também registra capturas do ZAP no Windows: título vazio rejeitado com `400` e tentativas repetidas de login bloqueadas com `429` e `Retry-After: 53`. O valor `53` era o tempo restante daquela janela, não uma configuração fixa.

Essas evidências locais são distintas dos arquivos de preparação listados acima. O documento do autor fornece o contexto de ambiente, data e capturas; o CSV sozinho não contém esses metadados. Os dois conjuntos de 67 asserções se sobrepõem e não devem ser somados como cenários independentes.

Para repetir a verificação no WSL, execute `mvn test`, inicie a API com `bash iniciar.sh` e, em outro terminal, rode `python3 verificar_entrega4.py https://localhost:8443 certs/localhost.crt`. O script sobrescreve o CSV da raiz. Registre data e ambiente de cada nova execução e oculte tokens e senhas nas capturas.
