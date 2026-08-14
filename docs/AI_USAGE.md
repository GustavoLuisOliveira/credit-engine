# Uso de Inteligência Artificial

Este projeto utiliza Inteligência Artificial como ferramenta de apoio
ao desenvolvimento.

## Ferramentas utilizadas

- ChatGPT
- Gemini

## Objetivo do uso

A IA foi utilizada como ferramenta de apoio para:
- discussão de arquitetura.
- análise de requisitos.
- revisão de decisões técnicas.
- geração de documentação.
- identificação de possíveis problemas de design.
- apoio na elaboração e validação de scripts de migração de banco de dados (Flyway).

## Registro de utilização

## [Feature] Configuração do Banco de Dados PostgreSQL (Docker & Env)
- **Branch**: `feature/postgres-setup`
- **Prompt**: "Definição do serviço PostgreSQL 17 via Docker Compose, suporte a charset UTF-8, parametrização por .env/.env.example, criação do script init-db.sql e validação do status healthy."
- **Contexto & Decisão**:
  - Escolhida a imagem `postgres:17-alpine` por ser leve, segura e estável.
  - Criado o script de inicialização `infra/database/init-db.sql` mapeado no volume do Docker para garantir a criação automática do banco `credit_engine_db` com `ENCODING = 'UTF8'` e `LC_COLLATE = 'pt_BR.UTF-8'`, assegurando suporte completo a acentuação e dados financeiros.
  - Implementado padrão de variáveis de ambiente com `.env.example` versionado e `.env` no `.gitignore`.
  - Configurado `healthcheck` no container para garantir que a base esteja pronta antes dos testes e das migrações da aplicação Spring.

---

### [Feature] Módulo de Gerenciamento de Moedas e Taxas de Câmbio (Currency & Exchange Rate)
- **Branch**: `feature/currency-context`
- **Prompt**: "Implementação, refatoração, validação, migração de banco de dados e testes unitários do módulo de moedas (Currency) e taxas de câmbio (ExchangeRate)."
- **Contexto & Decisão**:
  - **Domínio & Regras de Negócio**:
    - Definidas as regras de imutabilidade e conversão matemática de taxas de câmbio (`ExchangeRate.convert`).
    - Garantida a validação de formato ISO 4217 (3 letras maiúsculas) para moedas e impedimento de taxas nulas/negativas ou conversões para a mesma moeda origem/destino.
  - **Mapeamento JPA & Persistência**:
    - Criada `CurrencyEntity` herdando de `BaseAuditEntity` com chave primária natural (código ISO de 3 letras).
    - Criada `ExchangeRate` estendendo `BaseEntity` configurada como imutável (`updatable = false` e sem setters) para manter o histórico auditável no padrão Append-Only.
  - **Migração de Banco de Dados (Flyway Migration)**:
    - Desenvolvido script SQL de migração versionado (`V2__create_currency_and_exchange_rate_tables.sql`) para controle do esquema no banco de dados.
    - Criada a tabela `currency` com restrição PK de 3 caracteres (`CHAR(3)`) e colunas de auditoria (`created_at`, `updated_at`).
    - Criada a tabela `exchange_rate` com chaves estrangeiras (`FK`) vinculadas às moedas de origem (`from_currency_code`) e destino (`to_currency_code`), acompanhada de restrição `CHECK` para impedir taxas com valor igual ou inferior a zero.
    - Adicionados índices estratégicos para otimização de buscas por pares de moedas e consultas por ordenação temporal.
  - **DTOs & Bean Validation**:
    - Mapeados DTOs (`CurrencyRequest`, `CurrencyResponse`, `ExchangeRateRequest`, `ExchangeRateResponse`).
    - Aplicado Bean Validation com mensagens personalizadas e amigáveis em português (`@NotBlank`, `@Size`, `@DecimalMin(inclusive = false)` e `@PastOrPresent`).
  - **Camada de Serviço (`CurrencyServiceImpl` / `ExchangeRateServiceImpl`)**:
    - Aplicado `@Transactional(readOnly = true)` em nível de classe para desativar *dirty checking* no Hibernate e otimizar leituras no PostgreSQL, sobrescrevendo com `@Transactional` nos métodos de escrita.
    - Normalizados os códigos de moedas com `toUpperCase().trim()` antes do envio aos repositórios.
    - Tratamento de exceções com exceções customizadas de domínio (`DomainNotFoundException` e `DomainConflictException`).
  - **Testes Unitários (JUnit 5, AssertJ & Mockito)**:
    - Estruturados testes de domínio e serviço no padrão **AAA** (Arrange, Act, Assert) com anotações `@DisplayName` explicativas.
    - Utilizado Mockito (`@Mock`, `@ExtendWith(MockitoExtension.class)`) e `thenAnswer` para isolamento total da camada de persistência.
  - **Ajustes de Ambiente e Inicialização do Banco de Dados**:
    - Ajustado o script `infra/database/init-db.sql` para utilizar a collation `en_US.utf8` (compatível com o `template1` da imagem `postgres:17`), corrigindo o erro de inicialização do container.
    - Removida a declaração de `POSTGRES_DB` do `docker-compose.yml` para evitar conflito de execução e erro de banco existente (`database already exists`) ao executar o script inicial.
    - Ajustado o `healthcheck` do container no Compose para consultar a base padrão `postgres`, garantindo a validação da saúde do serviço durante a subida dos containers.
    - Atualizada a especificação do `docker-compose.yml` (remoção da propriedade obsoleta `version`).
  - **Padronização de Timezone & Serialização no Spring Boot**:
    - Migrados todos os tipos de data/hora do projeto de `OffsetDateTime` para `Instant`, garantindo o armazenamento de timestamp absoluto e imutável em UTC em toda a camada de domínio e persistência.
    - Configurado o fuso horário padrão da JVM para `America/Sao_Paulo` via `@PostConstruct` na classe `CreditEngineApiApplication`.
    - Implementado `InstantSerializer` customizado estendendo `StdSerializer<Instant>` da API Jackson 3 (`tools.jackson`), aplicando a formatação `DateTimeFormatter.ISO_OFFSET_DATE_TIME` ajustada explicitamente para a zona `America/Sao_Paulo`.
    - Registrado o módulo `SimpleModule` no contexto do Spring (`JacksonConfig`) para aplicar automaticamente a formatação do tipo `Instant` em toda a API.
    - Parametrizado `spring.jackson.time-zone=America/Sao_Paulo` no `application.properties` para alinhar as definições globais do framework.
  - **Documentação e Interface Interativa (Swagger/OpenAPI)**:
    - Configurada a integração do SpringDoc OpenAPI (`springdoc-openapi-starter-webmvc-ui`) no `application.properties`.
    - Mapeadas as rotas personalizadas da documentação (`/v3/api-docs`) e da interface do Swagger UI (`/swagger-ui.html`).
    - Ativada a ordenação por método HTTP e alfabética por tags para facilitar a navegação e testes de integração dos endpoints.