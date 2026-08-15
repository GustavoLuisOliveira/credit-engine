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

### [Feature] Configuração do Banco de Dados PostgreSQL (Docker & Env)
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
- **Prompts estratégicos utilizados**:
  - "Implementação, validação, migração de banco de dados e testes unitários do módulo de moedas (Currency) e taxas de câmbio (ExchangeRate) respeitando a convenção de pacotes e a arquitetura em camadas."
  - "Definição de regras de validação Bean Validation (@DecimalMin, @PastOrPresent, @NotBlank) com mensagens em português e tratamento de exceções de domínio."
  - "Ajuste na inicialização do container PostgreSQL (Docker Compose) tratando erros de collation UTF-8 e parâmetros de fuso horário global (UTC / Instant) no Spring Boot."
- **Onde a IA precisou de correção / pontos de atenção**:
  - **Normalização de inputs (`toUpperCase`)**: A verificação de existência no repositório (`existsById`) não utilizava normalização em alguns pontos de escrita, foi corrigido para garantir que `code.toUpperCase()` fosse aplicado em consultas antes da persistência.
  - **Remoção de propriedade obsoleta no Docker**: A IA manteve a chave `version` no `docker-compose.yml`, foi ajustado para remover o campo descontinuado e corrigir o erro de banco existente no `healthcheck`.
- **Análise crítica**:
  - **Onde economizou tempo**: Agilizou significativamente a criação de boilerplate repetitivo (DTOs, scripts de migração Flyway, anotações de validação e mapeamento de exceções customizadas). Também simplificou a configuração do Jackson Serializer para tipos de data `Instant`.
  - **Onde exigiu atenção humana**: Garantir que as entidades JPA e tabelas seguissem rigorosamente os conceitos de imutabilidade (*Append-Only* na `exchange_rate`), consistência dos fusos horários no Spring Boot e no banco de dados.
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

---

### [Feature] Módulo de Gerenciamento de Cedentes (Assignor)
- **Branch**: `feature/assignor-context`
- **Prompts estratégicos utilizados**:
  - "Criação, mapeamento JPA, isolamento de domínio e testes unitários do módulo de cedentes (Assignor) respeitando DDD, Clean Architecture e convenção de pacotes."
  - "Refatoração e documentação de testes unitários do Value Object Cnpj e da camada de serviço AssignorServiceImpl utilizando JUnit 5 (@DisplayName, @Nested, @ParameterizedTest), AssertJ e Mockito."
- **Onde a IA precisou de correção / pontos de atenção**:
  - **Bug no método `update` da Service**: Inicialmente a IA retornava a entidade desatualizada (`entity` carregada do banco antes das modificações) em vez do estado persistido (`saved`), fazendo com que o DTO de resposta ignorasse as alterações de `name`, `email` e `phone`. Foi corrigido para reidratar e retornar o objeto recém-salvo.
  - **Violação do encapsulamento de Domínio no `update`**: A IA tentou usar um construtor estático genérico que permitia alterar o CNPJ. Foi corrigido criando o método de instância `currentAssignor.update(...)`, garantindo que o `documentNumber` seja mantido e o `updatedAt` seja atualizado.
  - **Mapeamento de exceções e acoplamento**: O Service chamava a entidade JPA (`AssignorEntity`) em métodos auxiliares. A estrutura foi refatorada para expor apenas o modelo de domínio (`Assignor`) na camada de aplicação e utilizar a exceção de domínio `DomainConflictException` ao tentar alterar o CNPJ.
- **Análise crítica**:
  - **Onde economizou tempo**: Agilizou a escrita de boilerplates (DTOs Records, mapeamentos bidirecionais, JPA Annotations e anotações do Lombok), além da criação rápida do algoritmo de validação e testes do Value Object `Cnpj`.
  - **Onde exigiu atenção humana**: Garantir o isolamento estrito entre a entidade JPA (`AssignorEntity`) e o Modelo de Domínio (`Assignor`), assegurar a preservação dos dados de auditoria durante atualizações, criar a função `findDomainById` para evitar código duplicado e padronizar as mensagens/anotações dos testes do JUnit 5 com `@DisplayName` legível.
- **Contexto & Decisão**:
  - **Domínio & Regras de Negócio**:
    - Criado o Value Object `Cnpj` com validação de formato, Módulo 11 (dígitos verificadores), rejeição de sequências de dígitos repetidos/inválidos, formatação automática e imutabilidade.
    - Criado o modelo de domínio `Assignor` com construtor privado, métodos de fábrica estáticos (`create`, `restore`) e método de instância para atualização (`update`), encapsulando a regra de negócio que proíbe a alteração do CNPJ após o cadastro.
  - **Mapeamento JPA & Persistência**:
    - Criada a classe `AssignorEntity` estendendo `BaseEntity` e `BaseAuditEntity`, mapeando a tabela `assignor` com restrição de unicidade (`uq_assignor_document_number`).
    - Ajustado o nível de acesso dos setters de auditoria para `PROTECTED` em `BaseAuditEntity` para viabilizar a atualização de `updateAt` do domínio.
  - **DTOs & Camada de Aplicação**:
    - Criado o record `AssignorRequest` para transporte dos dados de entrada.
    - Criado o record `AssignorResponse` utilizando o método de fábrica declarativo `AssignorResponse.toResponse(assignor)` para conversão a partir do modelo de domínio, expondo o CNPJ formatado via mascara.
  - **Camada de Serviço (`AssignorServiceImpl`)**:
    - Configurado `@Transactional(readOnly = true)` na classe e `@Transactional` nos métodos de escrita (`create` e `update`).
    - Implementada checagem prévia de duplicidade com `existsByDocumentNumber` lançando `DomainConflictException`.
    - Isolada a manipulação de dados para usar apenas o modelo de domínio `Assignor` através do helper privado `findDomainById`.
  - **Testes Unitários (JUnit 5, AssertJ & Mockito)**:
    - Criados testes exaustivos para o Value Object `CnpjTest` utilizando `@ParameterizedTest` e `@ValueSource` para validação de bordas e sequências inválidas.
    - Estruturada a classe de testes de serviço `AssignorServiceImplTest` com organização em classes internas anotadas com `@Nested` (`CreateTests`, `UpdateTests`, `FindByIdTests`, `FindByDocumentNumberTests`).
    - Aplicada a anotação `@DisplayName` em todos os cenários para geração de relatórios limpos e rastreáveis na IDE/CI-CD.
    - Utilizado `ArgumentCaptor` do Mockito para validar o estado exato dos objetos de domínio enviados para persistência durante as operações de atualização.

---

### [Feature] Módulo de Gerenciamento de Títulos (Receivable)
- **Branch**: `feature/receivable-context`
- **Prompts estratégicos utilizados**:
  - "Criação da vertical slice completa (domain → infrastructure → application → web) do módulo de títulos, usando a slice já implementada de `currency` como referência de convenção de código."
  - "Criação do Value Object `Money` (domain.shared), compartilhado entre `Receivable`, `Settlement` e `SettlementItem`, sem acoplar o VO ao contexto `currency`."
  - "Criação do método `update` de `ReceivableServiceImpl`, usando como base o `update` já implementado em `AssignorServiceImpl` (mesmo padrão de buscar o domínio atual, validar campos imutáveis, aplicar `update()` de instância, persistir)."
  - "Adição de testes unitários para `Money`, `Receivable` e `ReceivableServiceImpl`."
- **Onde a IA precisou de correção / pontos de atenção**:
  - **Design inicial do `Money` divergia do padrão real do projeto**: a primeira versão referenciava o objeto `Currency` diretamente e usava uma exceção de domínio própria (`InvalidMoneyException`). Corrigido após inspeção do código de `currency` já implementado `Money` passou a referenciar a moeda por código `String` (mesmo padrão de `ExchangeRate.originCurrencyCode`) e a usar `IllegalArgumentException`/`NullPointerException` na validação, em vez de exceção própria.
  - **Bug no `Receivable.restore()`**: a IA validava `id` como não-nulo (`Objects.requireNonNull`). Isso quebrava com `NullPointerException` em testes que usam `save()` mockado retornando a própria entidade recém-construída (sem `id`, já que este só é atribuído pelo Hibernate no INSERT real). Corrigido removendo a validação de `id`, mantendo apenas a de `status`.
  - **Duplicação de validação de código de moeda**: a lógica de validar/normalizar código ISO 4217 estava repetida em `Currency` e `Money`. Extraída para `domain.shared.currency.CurrencyCodeValidator`.
  - **Endpoint de listagem sobrecarregado**: `findAll` inicialmente decidia entre listar tudo ou filtrar por cedente usando um `if` sobre um `@RequestParam(required = false)`. Refatorado para dois métodos explícitos (`findAll` e `findByAssignor`), usando `@GetMapping(params = "assignorId")` para desambiguar a rota no Spring, deixando a obrigatoriedade do parâmetro explícita na assinatura em vez de escondida num `if`.
- **Análise crítica**:
  - **Onde economizou tempo**: Agilizou a escrita de boilerplates (DTOs Records, mapeamentos bidirecionais, JPA Annotations e anotações do Lombok), além da criação rápida do algoritmo de validação e testes do Value Object `Money`.
  - **Onde exigiu atenção humana**: Garantir que o `update()` do domínio protegesse a invariante de status (`UNSETTLED`).
- **Contexto & Decisão**:
  - **Domínio & Regras de Negócio**:
    - Criado o Value Object `Money` (`domain.shared.money`), encapsulando `BigDecimal` + código de moeda, com escala fixa em 4 casas decimais e operações (`add`, `subtract`) que exigem mesma moeda.
    - Criado o modelo de domínio `Receivable` com construtor privado, fábricas estáticas (`create`, `restore`) e métodos de instância para transição de estado (`markAsSettled`, `cancel`) e atualização (`update`), este último protegendo a invariante de que só um título `UNSETTLED` pode ser editado.
    - Extraído `CurrencyCodeValidator` para `domain.shared.currency`, eliminando duplicação entre `Currency` e `Money`.
  - **Mapeamento JPA & Persistência**:
    - Criada `ReceivableEntity` estendendo `BaseEntity`, reaproveitando os enums de domínio `ReceivableType`/`ReceivableStatus` via `@Enumerated`.
    - Migration `V4__create_receivable_schema.sql`, FK para `assignor` e `currency`, e `CHECK` de `face_value > 0`.
  - **DTOs & Camada de Aplicação**:
    - Criados os records `ReceivableRequest`/`ReceivableResponse`.
    - Implementado `update` em `ReceivableServiceImpl` no mesmo padrão de `AssignorServiceImpl.update`: busca o domínio atual via `findDomainById` (helper privado reaproveitado também por `findById`), valida que `assignorId` e `type` não mudaram, aplica `currentReceivable.update(...)` e persiste.
  - **Camada de Serviço (`ReceivableServiceImpl`)**:
    - Configurado `@Transactional(readOnly = true)` na classe e `@Transactional` nos métodos de escrita (`create` e `update`).
    - Validação cruzada de existência do cedente via `AssignorRepository.existsById` antes da criação, lançando `DomainNotFoundException`.
  - **Testes Unitários (JUnit 5, AssertJ & Mockito)**:
    - Testes de `Money` e `Receivable` (domínio puro) cobrindo validação de invariantes e transições de estado inválidas.
    - Estruturada `ReceivableServiceImplTest` com organização em classes internas `@Nested`: `CreateTests`, `UpdateTests`, `FindByIdTests`, `FindByAssignorTests`.
    - Aplicada `@DisplayName` em todos os cenários para relatórios legíveis na IDE/CI-CD.

---

### [Feature] Motor de Precificação (Pricing Engine, Strategy Pattern)
- **Branch**: `feature/pricing-engine`
- **Prompts estratégicos utilizados**:
  - "Definição de ordem de implementação entre `pricing` e `settlement`, avaliando qual depende de qual antes de começar a codificar."
  - "Adição de comentários e exemplos de uso no método `calculate()` de `AbstractPricingStrategy`, cobrindo os dois `ReceivableType` suportados (Duplicata Mercantil e Cheque Pré-datado)."
  - "Adição de regra de negócio: prazo mínimo de 1 dia entre liquidação e vencimento, para garantir que o spread de risco seja de fato aplicado (evitar operação sem lucro)."
  - "Extensão da simulação de precificação (`PricingCalculationService.simulate`) para suportar conversão cambial opcional."
  - "Discussão e iteração sobre onde converter `baseRate`/`spreadRate` entre formato percentual (como digitado pelo operador) e fração decimal (como a fórmula de deságio exige), avaliadas três abordagens antes de fechar a definitiva."
  - "Geração da suíte de testes unitários (JUnit5 + Mockito + AssertJ) para `AbstractPricingStrategy`, `PricingParameter`, `PricingStrategyResolver` e `PricingCalculationServiceImpl`, cobrindo os cenários de negócio e os de erro."
- **Onde a IA precisou de correção / pontos de atenção**:
  - **Local de conversão percentual → fração decimal, revisado três vezes**: a primeira proposta da IA converteu na borda HTTP (`PricingParameterRequest`/`Response`), mantendo `PricingParameter` em fração. A segunda tentativa moveu a conversão para dentro do `AbstractPricingStrategy` (a pedido do desenvolvedor, para permitir que o operador sempre envie percentual e o banco armazene assim). Após reavaliação conjunta, a decisão final foi armazenar `baseRate`/`spreadRate` como percentual em `PricingParameter` (mesmo formato do banco) e criar os métodos `baseRateAsFraction()`/`spreadRateAsFraction()` no próprio domínio, mantendo o `AbstractPricingStrategy` puro (sempre fração) e sem depender de quem o chama saber fazer a conversão. Isso evita duplicar o `/100` no futuro `SettlementService`.
  - **Inconsistência de formato entre `PricingSimulationResponse` e `PricingParameterResponse`**: a primeira versão do `PricingCalculationServiceImpl` devolvia `result.getBaseRate()`/`getSpreadRate()` (fração, formato interno do cálculo) na resposta da simulação, enquanto o CRUD de parâmetros devolvia percentual, inconsistência identificada e corrigida trocando para `parameter.getBaseRate()`/`getSpreadRate()` (percentual, mesmo formato em toda a API).
  - **Teste desatualizado após regra de prazo mínimo**: `shouldReturnFaceValueAsPresentValueWhenTermIsZero` assumia que prazo zero era válido (presentValue = faceValue). Após a nova regra de negócio, esse cenário passou a lançar exceção, o teste foi reescrito (`shouldRejectSameDaySettlement`) e um novo teste foi adicionado (`shouldApplyDiscountForMinimumOneDayTerm`, prazo de exatamente 1 dia).
  - **Nomes de parâmetro ambíguos**: `calculate(Receivable, BigDecimal baseRate, BigDecimal spreadRate, LocalDate)` não deixava claro se o valor esperado era fração ou percentual, fonte de pelo menos dois bugs de teste ao longo da feature. Renomeado para `baseRateFraction`/`spreadRateFraction` na interface `PricingStrategy` e na implementação, tornando o contrato autoexplicativo.
- **Análise crítica**:
  - **Onde economizou tempo**: geração de boilerplate de testes (cenários de sucesso/erro, mocks, `@DisplayName`), e o cálculo manual dos valores esperados de deságio para os casos de teste.
  - **Onde exigiu atenção humana**: a decisão de arquitetura sobre formato percentual vs. fração exigiu três rodadas de discussão, a IA sugeriu inicialmente conversão na borda HTTP, mas o desenvolvedor preferiu manter o dado bruto do banco em percentual (mais legível em consulta SQL direta) e isolar a conversão matemática no domínio. Sem esse direcionamento explícito, a IA teria deixado a conversão espalhada entre DTO e service, criando um ponto a mais de duplicação quando `Settlement` for implementado.
  
    O teste original de `AbstractPricingStrategy` tratava prazo zero como caso válido (presentValue = faceValue, sem desconto). Apontei que isso permitia uma operação sem lucro para a mesa: com prazo zero, o fator de desconto é neutro `(1+totalRate)^0 = 1`, então o spread de risco nunca chega a ser efetivamente cobrado. A IA implementou a validação (prazo mínimo de 1 dia) e ajustou os testes a partir desse direcionamento.
- **Contexto & Decisão**:
  - **Domínio & Regras de Negócio**:
    - Criado `PricingStrategy` (interface) + `AbstractPricingStrategy` (Template Method com a fórmula de deságio) + `CommercialInvoicePricingStrategy`/`PostDatedCheckPricingStrategy` + `PricingStrategyResolver` (dispatch por `ReceivableType`, injeção da `List<PricingStrategy>` via Spring).
    - `AbstractPricingStrategy.calculate()` recebe `baseRateFraction`/`spreadRateFraction` já em fração decimal.
    - Regra de negócio adicionada: prazo entre `settlementDate` e `dueDate` deve ser de, no mínimo, 1 dia, cobrindo tanto recebível já vencido quanto vencimento na própria data de liquidação, já que em ambos os casos o fator de desconto seria neutro e o spread de risco nunca seria de fato cobrado.
    - `PricingParameter` (domínio) armazena `baseRate`/`spreadRate` em formato percentual (mesmo formato do `pricing_parameter` no banco) e expõe `baseRateAsFraction()`/`spreadRateAsFraction()` para conversão sob demanda, único ponto de conversão do sistema, reutilizável por qualquer consumidor futuro (`PricingCalculationService`, e futuramente `SettlementService`).
  - **Mapeamento JPA & Persistência**:
    - `PricingParameter` (entidade), `PricingParameterRepository` com `findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc` (busca a taxa vigente numa data de referência) e `findByReceivableTypeOrderByEffectiveDateDesc` (histórico).
    - Migration `V5__create_pricing_parameter_schema.sql`: tabela append-only (todas as colunas `updatable = false`), sem FK (o `receivable_type` é um enum fechado do domínio, validado via `CHECK` constraint), índice composto `(receivable_type, effective_date DESC)` para a busca de taxa vigente.
  - **DTOs & Camada de Aplicação**:
    - `PricingParameterRequest`/`Response`: `baseRate`/`spreadRate` em percentual, validados com `@DecimalMin`/`@DecimalMax` (0 a 100).
    - `PricingSimulationResponse` estendido com `targetCurrencyCode`, `exchangeRateUsed` e `convertedAmount` (todos `null` quando não há conversão cambial), mantendo o mesmo formato percentual do `PricingParameterResponse` para `baseRate`/`spreadRate`.
    - `PricingCalculationServiceImpl.simulate()` passou a aceitar `targetCurrencyCode` opcional: quando informado e diferente da moeda original do título, consulta `ExchangeRateService.findLatestRate(...)` e converte o `presentValue` (já com deságio) para a moeda alvo. A conversão cambial é sempre aplicada depois do cálculo de deságio, nunca misturada com ele, conforme já definido na arquitetura de domínio.
    - Avaliada e descartada a alternativa de expor um `findDomainById` na interface pública `ReceivableService` para reaproveitar em `PricingCalculationServiceImpl`, mantido o acesso direto via `ReceivableRepository` + `ReceivableMapper`, já que não há regra de negócio nova a centralizar e a interface pública do service deve permanecer 100% DTO-oriented (contrato consumido pelo controller).
  - **Testes Unitários (JUnit 5, AssertJ & Mockito)**:
    - `AbstractPricingStrategy`/`CommercialInvoicePricingStrategyTest`: cenários de deságio para prazo de 1 mês, rejeição de prazo zero e de recebível vencido, e caso de borda no prazo mínimo válido (1 dia).
    - `PricingParameterTest`: validação de `totalRate()`, `baseRateAsFraction()`/`spreadRateAsFraction()`, e rejeição de taxas negativas/tipo nulo.
    - `PricingStrategyResolverTest`: resolução correta por `ReceivableType` e exceção quando não há estratégia registrada.
    - `PricingCalculationServiceImplTest`: simulação sem conversão cambial, sem conversão quando moeda alvo é igual à original, com conversão cambial aplicada sobre o valor presente, resolução da estratégia correta para `POST_DATED_CHECK` (prova que o resolver não "vaza" para a estratégia errada), e os dois cenários de exceção (recebível não encontrado / parâmetro de precificação não configurado).