# Credit Engine

Plataforma de cessão de crédito multimoedas para gestão de recebíveis e liquidação.

## 1. Contexto de Negócio

O Credit Engine atende operações de FIDC (Fundo de Investimento em Direitos Creditórios), que adquirem recebíveis (duplicatas, cheques pré-datados) de empresas cedentes. Com a operação passando a lidar com caixa multimoedas (BRL e USD), o sistema resolve o seguinte problema:

- Receber um lote de recebíveis de um cedente.
- Precificar cada recebível aplicando deságio (desconto) conforme o risco do tipo de ativo.
- Converter o valor para a moeda de pagamento escolhida pelo cedente, quando necessário.
- Registrar a liquidação de forma imutável e auditável.

## 2. Arquitetura

O backend segue **Clean Architecture / DDD** com separação estrita em camadas verticais por contexto de domínio:

```
domain.{model,pricing,shared} -> infrastructure.persistence.{entity,repository,mapper} -> application.{dto,service} -> web.{controller,handler}
```

Princípios aplicados:

- Modelos de domínio como POJOs puros, sem anotações JPA. As anotações JPA ficam isoladas nas classes de entidade.
- Referências entre agregados feitas por UUID/String, sem `@ManyToOne` cruzando limites de agregado.
- Entidades de auditoria imutáveis (append only), com `created_at` protegido contra alteração via trigger no banco.
- DTOs como Java records.
- Motor de precificação desacoplado da persistência via **Strategy Pattern**, permitindo adicionar novos tipos de recebível sem alterar o cálculo existente.

### Contextos de domínio implementados

| Contexto | Descrição |
|---|---|
| Currency / ExchangeRate | Moedas suportadas e histórico de cotações cambiais |
| Assignor (Cedente) | Empresas detentoras dos títulos de crédito |
| Receivable (Recebível) | Títulos a serem precificados (Duplicata Mercantil, Cheque Pré-datado) |
| PricingParameter | Parâmetros de taxa (base e spread) por tipo de recebível, versionados de forma append only |
| Pricing Engine | Cálculo do valor presente via Strategy Pattern, com parâmetros de taxa versionados |
| Settlement | Liquidação em lote, com fotografia de auditoria imutável por item |

Diagramas ER e detalhamento completo da modelagem estão em `/docs`.

## 3. Stack Tecnológica

**Backend**
- Java 21, Spring Boot, Maven
- PostgreSQL 17
- Flyway (migrations)
- Spring Data JPA, Lombok
- JUnit 5, Mockito, AssertJ
- springdoc-openapi (Swagger UI)

**Frontend**
- React, TypeScript
- PrimeReact, PrimeFlex

**Infraestrutura**
- Docker e Docker Compose

## 4. Como Rodar o Projeto

### Pré-requisitos

- Docker e Docker Compose instalados.

### Passo a passo

1. Clone o repositório:

```bash
git clone <url-do-repositorio>
cd credit-engine
```

2. Crie o arquivo `.env` na raiz do projeto com as variáveis abaixo (ajuste os valores conforme necessário):

```env
POSTGRES_USER=credit_engine
POSTGRES_PASSWORD=credit_engine
POSTGRES_DB=credit_engine_db
POSTGRES_PORT=5433
PGCLIENTENCODING=UTF8

APP_CORS_ALLOWED_ORIGINS=http://localhost:80

VITE_API_URL=http://localhost:8080/api
FRONTEND_PORT=80
```

3. Suba os containers:

```bash
docker compose up --build -d
```

4. Aguarde os três serviços ficarem saudáveis. O backend só inicializa após o healthcheck do Postgres passar, e roda as migrations Flyway automaticamente na subida. O build da imagem do backend executa os testes unitários antes de empacotar o jar, se algum teste falhar o build é interrompido e o container não sobe.

5. Acesse:

| Serviço | URL |
|---|---|
| Frontend | http://localhost |
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |

### Encerrando o ambiente

```bash
docker compose down
```

Para remover também os dados persistidos do banco:

```bash
docker compose down -v
```

## 5. Principais Endpoints

Documentação completa via Swagger UI. Alguns exemplos:

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/currencies` | Cadastra uma moeda |
| GET | `/api/currencies` | Lista todas as moedas cadastradas |
| GET | `/api/currencies/{code}` | Busca uma moeda pelo código |
| POST | `/api/exchange-rates` | Registra uma cotação de câmbio |
| GET | `/api/exchange-rates/latest?origin=&destination=` | Busca a cotação mais recente entre duas moedas |
| POST | `/api/assignors` | Cadastra um cedente |
| GET | `/api/assignors?documentNumber=` | Busca cedente(s) pelo número de documento |
| PUT | `/api/assignors/{id}` | Atualiza um cedente |
| POST | `/api/receivables` | Cadastra um recebível |
| GET | `/api/receivables?assignorId=` | Lista recebíveis de um cedente |
| PUT | `/api/receivables/{id}` | Atualiza um recebível |
| GET | `/api/receivables/{receivableId}/pricing-simulation` | Simula o cálculo de precificação sem persistir |
| POST | `/api/pricing-parameters` | Registra um novo parâmetro de taxa (append only, por tipo de recebível) |
| GET | `/api/pricing-parameters/{receivableType}/current` | Consulta o parâmetro de taxa vigente |
| GET | `/api/pricing-parameters/{receivableType}/history` | Consulta o histórico de parâmetros de taxa |
| POST | `/api/settlements` | Executa a liquidação de um lote de recebíveis |
| GET | `/api/settlements/{id}` | Consulta uma liquidação pelo id |
| GET | `/api/settlements?assignorId=` | Lista liquidações de um cedente |
| GET | `/api/settlements/extract` | Extrato de liquidações com filtros (cedente, moeda, período) e paginação server side |

## 6. Testes

Cobertura de testes unitários concentrada nas regras de negócio, especialmente no motor de precificação (Strategy Pattern) e nos serviços de liquidação. Organizados com `@Nested` por cenário.

```bash
cd credit-engine-api
./mvnw test
```

## 7. Uso de IA

O uso de IA no desenvolvimento deste projeto está documentado em [`AI_USAGE.md`](./docs/AI_USAGE.md), incluindo prompts estratégicos utilizados, correções aplicadas sobre código gerado e análise crítica do processo.

## 8. Fluxo de Git

- Commits seguem o padrão **Conventional Commits** (`feat`, `fix`, `refactor`, `docs`, `chore`).
- Cada feature é desenvolvida em branch própria e integrada via Pull Request.