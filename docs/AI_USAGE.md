# Uso de Inteligência Artificial

Este projeto utiliza Inteligência Artificial como ferramenta de apoio
ao desenvolvimento.

## Ferramentas utilizadas

- ChatGPT

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