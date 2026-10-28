# Referências e citações — Entrega 4

Reúne nove fontes e seis tópicos de citação para o referencial. Os metadados e os trechos citados foram conferidos tecnicamente em 27/10/2026; o campo “Acesso técnico” abaixo não representa a data de leitura do aluno. O documento de instruções fornecido pelo autor informa leitura em 11/09/2026 para R1, R2, R3, R6 e R7, e em 27/10/2026 para R4 e R5. A leitura de R8 e R9 ainda consta como “a registrar”. Ao inserir as referências no TCC, use as datas de consulta efetivas e revise a formatação exigida pelo curso.

## R1

OWASP FOUNDATION. API1:2023 Broken Object Level Authorization. OWASP API Security Top 10, 2023.

Disponível em: <https://api-security.owasp.org/editions/2023/en/0xa1-broken-object-level-authorization/>. Acesso técnico em: 27 out. 2026.

Relatório técnico; autorização por objeto. Fundamenta a checagem da permissão sobre cada tarefa. Não estabelece neste registro percentual de ataques.

## R2

HAKIM, Muhammad Ikhwanul; NUGROHO, Radityo Adi; NUGRAHADI, Dodon Turianto; HERTENO, Rudy; SAPUTRO, Setyo Wahyu. Performance Analysis of the Fuzzing Method in Detecting API Vulnerabilities in Mobile Healthcare Application X Based on OWASP API Security Top 10. Telematika, v. 19, n. 1, p. 11–21, fev. 2026. DOI: 10.35671/telematika.v19i1.3149.

Disponível em: <https://ejournal.amikompurwokerto.ac.id/index.php/telematika/article/view/3149>. Acesso técnico em: 27 out. 2026.

Artigo científico; metodologia de testes. O resumo descreve experimento grey-box com FFUF e 33 endpoints. Apoia a discussão sobre validação de achados; seus resultados não são transferidos para esta API.

## R3

DALIMUNTHE, Syabdan; PUTRA, Emansa Hasri; RIDHA, Muhammad Arif Fadhly. Restful API Security Using JSON Web Token (JWT) With HMAC-Sha512 Algorithm in Session Management. IT Journal Research and Development, v. 8, n. 1, p. 81–94, 2023. DOI: 10.25299/itjrd.2023.12029.

Disponível em: <https://journal.uir.ac.id/index.php/ITJRD/article/view/12029>. Acesso técnico em: 27 out. 2026.

Artigo científico; implementação de JWT. O resumo trata de JWT com HMAC-SHA512 armazenado em cookies e compara algoritmos HMAC. É trabalho relacionado; difere do uso de HS256 e Bearer neste experimento.

## R4

JONES, Michael; BRADLEY, John; SAKIMURA, Nat. JSON Web Token (JWT). RFC 7519. IETF, maio 2015. DOI: 10.17487/RFC7519.

Disponível em: <https://www.rfc-editor.org/rfc/rfc7519.html>. Acesso técnico em: 27 out. 2026.

Norma técnica; claims JWT. As seções 4.1.2 e 4.1.4 apoiam a interpretação de identidade e expiração.

## R5

SHEFFER, Yaron; HARDT, Dick; JONES, Michael. JSON Web Token Best Current Practices. RFC 8725. BCP 225. IETF, fev. 2020. DOI: 10.17487/RFC8725.

Disponível em: <https://www.rfc-editor.org/rfc/rfc8725.html>. Acesso técnico em: 27 out. 2026.

Boa prática normativa; validação criptográfica. As seções 3.1 e 3.5 apoiam a restrição de algoritmo e o uso de chave aleatória.

## R6

ZAP DEV TEAM. X-Content-Type-Options Header Missing. ZAP Documentation, [s.d.].

Disponível em: <https://www.zaproxy.org/docs/alerts/10021/>. Acesso técnico em: 27 out. 2026.

Documentação oficial; análise passiva. Define regra 10021, risco baixo, CWE-693 e a recomendação de nosniff.

## R7

OWASP FOUNDATION. REST Security Cheat Sheet. OWASP Cheat Sheet Series, [s.d.].

Disponível em: <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html>. Acesso técnico em: 27 out. 2026.

Guia técnico; proteção de APIs. Orienta controle de acesso por endpoint, HTTPS e validação de tokens. Apoia também a delimitação das proteções ainda pendentes.

## R8

OWASP FOUNDATION. Input Validation Cheat Sheet. OWASP Cheat Sheet Series, [s.d.].

Disponível em: <https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html>. Acesso técnico em: 27 out. 2026.

Guia técnico; validação de entradas. Fundamenta limites de campos e de corpo, validação no servidor e rejeição de campos não previstos.

## R9

NOTTINGHAM, Mark; FIELDING, Roy. Additional HTTP Status Codes. RFC 6585. IETF, abr. 2012. DOI: 10.17487/RFC6585.

Disponível em: <https://www.rfc-editor.org/rfc/rfc6585.html>. Acesso técnico em: 27 out. 2026.

Norma técnica; limitação de requisições. Seção 4, p. 3: define status 429, possibilidade de Retry-After e proibição de cache dessas respostas.

## Lista com citações diretas

| Tópico | Fonte | Citação direta e localização |
|---|---|---|
| Autorização em nível de objeto | R1 — OWASP, 2023 | “The checks should validate that the logged-in user has permissions” (seção Is the API Vulnerable?). |
| Metodologia de teste por fuzzing | R2 — Hakim et al., 2026 | “employs a grey-box controlled experimental design to assess the effectiveness of automated HTTP fuzzing” (Abstract). |
| Identidade representada pelo JWT | R4 — Jones; Bradley; Sakimura, 2015 | “The "sub" (subject) claim identifies the principal” (seção 4.1.2, p. 9). |
| Verificação criptográfica de JWT | R5 — Sheffer; Hardt; Jones, 2020 | “each key MUST be used with exactly one algorithm” (seção 3.1). |
| Validação de entradas no servidor | R8 — OWASP FOUNDATION, [s.d.] | “Define what the application accepts and reject values outside those rules.” (seção Allowlist vs Denylist). |
| Limitação de requisições HTTP | R9 — Nottingham; Fielding, 2012 | “The 429 status code indicates that the user has sent too many requests in a given amount of time” (seção 4, p. 3). |

Os trechos estão no idioma original. Fuzzing é trabalho relacionado; não foi executado automaticamente nesta API. Não há estatística de 40% ou de 9% atribuída a estas fontes. HTTPS e tratamento de erros também são fundamentados pela REST Security Cheat Sheet (R7).
