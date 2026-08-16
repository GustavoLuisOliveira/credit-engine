# Uso de Inteligência Artificial

Este projeto utiliza Inteligência Artificial como ferramenta de apoio
ao desenvolvimento.

## Ferramentas utilizadas

- ChatGPT
- Gemini
- Claude

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

--- 

### [Feature] Contexto de Liquidação (Settlement, Settlement Item)
- **Branch**: `feature/settlement-context`
- **Prompts estratégicos utilizados**:
  - "Implementação da vertical slice completa de Settlement/SettlementItem (domain, infrastructure, application, web), seguindo o padrão já estabelecido pelas slices anteriores (Currency, Assignor, Receivable/Pricing)."
  - "Avaliação de onde armazenar o prazo entre liquidação e vencimento: dias corridos (auditoria legível) vs. o valor fracionário em meses efetivamente usado no expoente da fórmula de deságio."
  - "Verificação se a data usada como base da precificação do lote (valuationDate) estava sendo persistida em algum lugar, ou se só existia durante a execução da requisição."
  - "Rename de campo para eliminar ambiguidade semântica entre a data-base de precificação `(valuationDate)` e o timestamp de auditoria `(settlementDateTime)` de quando o lote foi executado."
  - "Geração da suíte de testes unitários (JUnit5 + Mockito + AssertJ) para Settlement, SettlementItem e SettlementServiceImpl, organizada em classes @Nested por cenário (pré-condições, same-currency, cross-currency, lote, concorrência, consultas)."
  - "Revisão de nomes de métodos privados e simplificação de construtores de entidade após identificar parâmetros copiados de outras entidades sem necessidade real no contexto do Settlement."
- **Onde a IA precisou de correção / pontos de atenção**:
  - **`term` (dias) vs. prazo fracionário usado na fórmula, revisado após feedback**: a primeira proposta da IA foi reconverter `dias / 30` sob demanda, sem coluna própria. Apontei, que essa reconversão perde precisão (o arredondamento `HALF_EVEN` já aplicado ao valor fracionário faz `term_months * 30` divergir de `term` na casa decimal). A decisão final foi congelar os dois valores separadamente: `term` (dias corridos, fato de auditoria legível) e `term_months` (o valor fracionário exato que alimentou o expoente da fórmula), ambos gravados no `settlement_item`.
  - **`valuationDate` não estava sendo persistido em lugar nenhum**: inicialmente esse campo só existia durante a execução do `execute()`, usado para calcular `term`/`term_months`/taxas/valores do item e depois descartado. Questionei se isso não configurava perda de dado, e a resposta foi sim: a única forma de reconstruir "com que data-base esse lote foi precificado" seria um JOIN entre `settlement_item.term` e `receivable.due_date`, sem garantia de fidelidade para fins de auditoria/Extrato de Liquidação. Adicionada a coluna `valuation_date DATE NOT NULL` no cabeçalho `settlement` (não no item, por ser um dado do lote inteiro, não do recebível individual).
  - **Nome de campo ambíguo (`settlementDate`)**: colidia semanticamente com `Settlement.settlementDateTime` (timestamp de quando o lote foi executado), apesar de representarem conceitos diferentes. Renomeado para `valuationDate`, termo padrão de mercado financeiro para a data-base de um cálculo de valor presente, eliminando a ambiguidade textual entre os dois campos.
  - **Construtor de `SettlementEntity` com parâmetro `id` desnecessário**: copiado do padrão usado em entidades que suportam `update()` (como `ReceivableEntity`), sem que o `Settlement` tivesse essa necessidade real (é write-once, nunca reidratado com um domínio que já possua `id`).
- **Análise crítica**:
  - **Onde economizou tempo**: geração do boilerplate da vertical slice inteira (domain, entidade, mapper, repositório, DTOs, service, controller), e de testes (incluindo os cenários de concorrência e conversão cambial).
  - **Onde exigiu atenção humana**: a IA não identificou de imediato duas lacunas reais de auditoria (`term_months` e `valuation_date`), ambas só corrigidas porque o questionei explicitamente "esse dado está sendo perdido?" antes de aceitar a primeira proposta como definitiva. Sem esse questionamento, a reconstrução desses valores dependeria de JOINs entre tabelas, e o sistema teria uma fonte de discrepância silenciosa entre o que a mesa de operação acredita ter usado para precificar e o que de fato é reconstruível a partir dos dados brutos armazenados.
- **Contexto & Decisão**:
  - **Domínio & Regras de Negócio**:
    - Criados `Settlement` (aggregate root, cabeçalho de liquidação) e `SettlementItem` (fotografia de auditoria imutável) em `domain.model.settlement`, seguindo o mesmo estilo de POJOs sem anotações JPA já usado em `Receivable`/`PricingParameter`.
    - `Settlement.totalFaceValue`/`totalDiscountAmount`/`totalNetAmount` sempre expressos na moeda alvo (`targetCurrency`, derivada dos próprios totais), nunca na moeda original dos títulos: um lote pode misturar recebíveis em BRL e USD, e a moeda alvo é a única unidade comum em que os valores podem ser somados.
    - `Settlement.valuationDate`: data-base que alimentou a fórmula de deságio de todos os itens do lote, congelada no cabeçalho por ser um dado do lote inteiro (uma única execução usa uma única data-base para todos os recebíveis).
    - `SettlementItem.term` (dias corridos, auditoria legível) e `termMonths` (fração usada no expoente da fórmula) congelados separadamente, sem perda de precisão na reconstrução.
    - `SettlementItem.baseRate`/`spreadRate` gravam a fração decimal que efetivamente alimentou a fórmula, diferente do formato percentual usado em `pricing_parameter`.
    - Proteção contra liquidação em duplicidade sob concorrência: a `UNIQUE` constraint em `settlement_item.receivable_id` é a garantia definitiva; a checagem otimista via `existsByReceivableId` é apenas um fail-fast que economiza cálculo desnecessário.
  - **Mapeamento JPA & Persistência**:
    - Migration `V6__create_settlement_schema.sql`: tabelas `settlement` (cabeçalho) e `settlement_item` (fotografia de auditoria), colunas de negócio `updatable = false` (imutável/append-only), coluna gerada `total_rate` (`base_rate + spread_rate`) no `settlement_item`, `UNIQUE` em `receivable_id`.
    - Colunas `term_months NUMERIC(10,6)` e `valuation_date DATE NOT NULL` adicionadas após revisão, editadas diretamente na própria V6.
    - `SettlementEntity`/`SettlementItemEntity` sem `@ManyToOne`/`@OneToMany` entre agregados, seguindo o padrão do projeto de referenciar por UUID/String puro, evitando acoplamento via JPA.
    - Persistência do item via `saveAndFlush` (não `save`), para que a violação da `UNIQUE` constraint estoure dentro do método da service, traduzível para `DomainConflictException`, em vez de vazar como erro genérico só no commit final da transação.
  - **DTOs & Camada de Aplicação**:
    - `SettlementRequest`: `assignorId`, `valuationDate`, `targetCurrencyCode`, `receivableIds`.
    - `SettlementResponse`/`SettlementItemResponse`: expõem `valuationDate`, `term`, `termMonths` e `totalRate` calculado (`baseRate + spreadRate`).
    - `SettlementServiceImpl.execute()`: duas passadas, calcula preço e conversão cambial de cada recebível sem persistir nada (`calculateItem`), depois persiste o cabeçalho (para gerar o `settlementId`) e por fim persiste cada item e marca o recebível como liquidado (`persistItemAndSettleReceivable`), tudo dentro de uma única `@Transactional`.
    - Conversão cambial: a mesma cotação (`exchangeRateUsed`) é aplicada a `faceValue`, `discountAmount` e `presentValue` do item, garantindo que os totais do `Settlement` (sempre na `targetCurrency`) somem de forma coerente entre si.
  - **Testes Unitários (JUnit 5, AssertJ & Mockito)**:
    - `SettlementTest`/`SettlementItemTest`: validações de invariantes que espelham os `CHECK` da migration (`term > 0`, taxas `>= 0`, valores positivos, câmbio `> 0`, moedas coerentes entre os `Money` do mesmo agregado).
    - `SettlementServiceImplTest`, organizado em `@Nested` por cenário (`ExecutePreconditions`, `SameCurrencyExecution`, `CrossCurrencyExecution`, `BatchExecution`, `ConcurrencyAndIntegrity`, `SettlementQueries`): cobre as validações de pré-condição, o fluxo same-currency (sem chamar `ExchangeRateService`), o fluxo cross-currency (conversão consistente entre `faceValue`/`discountAmount`/`presentValue`), a acumulação de totais em lote com múltiplas moedas, a tradução de `DataIntegrityViolationException` em `DomainConflictException` sob concorrência (sem marcar o recebível como liquidado nesse caso), e as consultas `findById`/`findByAssignor`.

---

### [Feature] Moedas e Cambio (Currency, ExchangeRate) - Frontend
- **Branch**: `feature/currency-ui`
- **Prompts estrategicos utilizados**:
  - "Extracão dos contratos reais de API a partir do backend: controllers, DTOs, enums e `GlobalExceptionHandler`, para gerar services e DTOs do frontend sem inventar formato de payload."
  - "Ajuste incremental de UX no painel de cotações: busca automatica ao preencher os dois filtros (remocão do botao Buscar) e exclusão da moeda de origem das opcões de destino, aplicado tanto na consulta quanto no formulario de cadastro."
- **Onde a IA precisou de correção / pontos de atenção**:
  - **PrimeReact 11 pareceria "mais atual", mas nao era a escolha certa**: a IA levantou que a versao mais recente era a 11.1.0, porem investigando identificou que reconstroi o motor de temas do zero (tokens em vez do `theme.css` SASS que o projeto de referencia usa). O usuario optou por travar em 10.9.8, evitando quebrar o setup de tema ja validado.
  - **Estado de moedas duplicado**: `Currencies` e a pagina que a continha chamavam `useCurrencies()` cada uma por conta propria, gerando dois fetches e duas listas fora de sincronia entre si (uma usada no grid, outra nos selects de cotação). Corrigido subindo o estado para o componente pai e passando via props.
  - **Resultado de cotação ficava obsoleto**: trocar a moeda de origem ou destino sem re-executar a busca mantinha na tela o resultado do par anterior, como se fosse do par atual. Corrigido com uma função `reset()` explicita no hook, disparada sempre que os filtros mudam.
- **Analise critica**:
  - **Onde economizou tempo**: leitura e mapeamento do projeto do backend para extrair convenções e contratos reais, em vez de ter que descrever cada endpoint e padrao manualmente; geração do scaffold completo (config, api client, Context, componentes compartilhados) validado a cada etapa.
  - **Onde exigiu atenção humana**: O bug de estado duplicado e o de cotação obsoleta.
- **Contexto & Decisao**:
  - **Arquitetura & Convenções**:
    - Estrutura de pastas por contexto de dominio (`services/currency`, `hooks/currency`, `components/currency`), espelhando o padrao ja usado no backend (`domain.model.currency`, `application.dto.currency` etc).
    - Estado de servidor via hooks customizados (`useState`/`useEffect`/`useCallback`), sem biblioteca de cache externa (TanStack Query, SWR), seguindo o padrao real do projeto de referencia em vez da recomendação inicial da IA.
    - Validação de formulario manual, exportada junto ao DTO (`validateCurrencyRequest`, `validateExchangeRateRequest`), sem Zod nem React Hook Form, mesmo padrao da referencia.
    - Metodos, classes, componentes, props e tipos em ingles (`create`, `findAll`, `findLatestRate`, `TextInput`, `SelectInput`); textos exibidos ao usuario (labels, mensagens de toast, titulos de card) em portugues.
  - **Componentes & UX**:
    - Regra de negocio de UX aplicada tanto na consulta (`ExchangeRates`) quanto no cadastro (`FormExchangeRate`): a moeda selecionada como origem e removida das opções de destino, e o destino e limpo automaticamente se coincidir com a nova origem escolhida.
  - **Configuração Backend**:
    - `CorsConfig` criado no backend (`infrastructure.config`), com origem configuravel via `app.cors.allowed-origins` (relaxed binding para `APP_CORS_ALLOWED_ORIGINS`), permitindo que o `docker-compose.yml` injete a origem do frontend a partir do `.env` sem alterar codigo Java.

---

### [Feature] Painel do Operador (Assignor, Receivable, Pricing Simulation) - Frontend
- **Branch**: `feature/operator-panel`
- **Prompts estrategicos utilizados**:
  - "Implementação do Painel do Operador seguindo o padrão já estabelecido no scaffold existente (client HTTP, hooks com toast, DTOs com `validate*Request`, `FormDialogContainer`, inputs compartilhados), a partir da leitura do `OpenApi.json` real do backend."
  - "Separação de um hook único (`useOperatorPanel`) em dois hooks especializados por responsabilidade (`useReceivables` para persistência do recebível, `usePricingSimulation` para a chamada de simulação)."
  - "Adição de listagem de recebíveis em aberto do cedente com simulação em lote, e redesenho de UX movendo Data de Referência e Moeda de Liquidação para uma seção compartilhada no topo, com o formulário de recebível novo escondido atrás de um botão."
  - "Refatoração do fluxo de simulação em lote: ao salvar um recebível novo, selecioná-lo junto aos já selecionados e simular só ele, sem refazer a simulação dos demais; usar somente `BatchPrincingSimulationResults` (remoção da simulação individual isolada); deixar o `ReceivableForm` controlado apenas pelo `ReceivableList`; limpar o `OperatorPanel`."
  - "Ajuste de assinatura do `save` para receber `receivableId` como parâmetro opcional em vez de guardá-lo como estado interno do hook, evitando que o hook decida create/update por conta própria."
  - "Simetria entre marcar e desmarcar um recebível na lista: marcar simula só o item novo, desmarcar descarta só o resultado daquele item, sem afetar os demais já simulados."
  - "Recalcular os resultados já simulados quando a Data de Referência ou a Moeda de Liquidação mudam, já que ambas afetam o valor de todo mundo que já tem resultado."
- **Onde a IA precisou de correção / pontos de atenção**:
  - **Conflito de contrato identificado antes de codar**: o endpoint `GET /receivables/{id}/pricing-simulation` exige um `Receivable` já persistido, inviabilizando simulação em tempo real via debounce a partir de dados soltos do formulário. Resolvido com fluxo "Simular": primeiro clique faz `POST`, cliques seguintes no mesmo recebível fazem `PUT`, evitando duplicar registros a cada ajuste.
  - **Ausência de endpoints de listagem geral e de simulação em lote**: não existe `GET /assignors` sem filtro nem simulação em lote no backend. Adaptado com busca de cedente por CNPJ (com cadastro inline se não encontrado) e loop de simulações individuais por recebível selecionado (`Promise.all`), sem somar os resultados.
  - **Bug visual (moeda vazia)**: quando a moeda de liquidação escolhida era igual à moeda do título, `targetCurrencyCode` e `convertedAmount` vinham vazios na resposta, deixando "Cambio (BRL para )" e "Valor Líquido a Receber" em branco. Corrigido com fallback no frontend (`targetCurrencyCode || currencyCode`, `convertedAmount ?? presentValue`).
  - **Rótulo incorreto no resultado da simulação**: o campo `term` estava rotulado como "dia(s)", mas as taxas são a.m. (ao mês), retornando meses fracionados. Corrigido o rótulo para "mes(es)".
  - **Bug de estado singleton no `save`**: o `receivableId` guardado internamente no hook (`useReceivables`) fazia o segundo recebível criado virar um `PUT` no primeiro (erro "Tipo do título não pode ser alterado após o cadastro"). Corrigido primeiro isolando o reset do id de rascunho, depois eliminando esse estado por completo: `receivableId` passou a ser parâmetro opcional do `save`, e quem chama decide `create` ou `update`.
  - **`upsertResults` apagando simulações de outros recebíveis**: a primeira versão filtrava `prev` mantendo só os ids presentes na atualização atual, descartando tudo que não fazia parte da chamada. Corrigido para só sobrescrever/inserir os ids da atualização, preservando o restante do Map.
  - **Resultado de item desmarcado continuava exibido**: nada disparava a remoção quando o operador desmarcava um recebível na tabela, já que o `remove` só existia isolado, sem uso. Adicionado o diff de seleção (`added`/`removed`) no `onSelectionChange` do `ReceivableList`, chamando `remove` para os ids retirados.
  - **Proposta inicial de resimular ao trocar moeda/data usava `useEffect` com `useRef` para evitar loop**: rejeitada por ser gambiarra (efeito lendo estado próprio via ref para não entrar em loop). Substituída por disparo direto nos handlers `onValuationDateChange`/`onTargetCurrencyCodeChange`, sem `useEffect`, usando os `receivableId`s já presentes em `results` no momento do evento.
- **Analise critica**:
  - **Onde economizou tempo**: leitura fiel do `OpenApi.json` e do scaffold existente evitou inconsistência de contrato e de estilo; reaproveitamento de componentes compartilhados (`FormDialogContainer`, inputs, padrão de `DataTable` já usado em `Currencies`) acelerou a montagem das novas telas.
  - **Onde exigiu atenção humana**: os dois bugs de exibição (moeda vazia e rótulo de prazo) só foram percebidos ao testar visualmente no navegador, mostrando que suposições sobre o formato exato da resposta do backend (campo sempre preenchido, unidade sempre em dias) precisam ser validadas com dados reais, não só com o schema do OpenAPI.
- **Contexto & Decisao**:
  - **Arquitetura & Convenções**:
    - Estrutura por contexto (`services/assignor`, `services/receivable`, `services/pricing`), espelhando o padrão do backend.
    - Hooks especializados por responsabilidade (`useAssignors`, `useReceivables`, `usePricingSimulation`, `useBatchPricingSimulation`) em vez de um hook único orquestrando tudo.
    - CNPJ tratado como digits-only na comunicação com o backend (`stripDocumentNumberMask`), mesmo padrão já estabelecido para o Assignor.
    - `dueDate` convertido explicitamente para `yyyy-MM-dd` (`DateUtils.toLocalDateString`) antes do payload, já que e um `LocalDate` no backend e `JSON.stringify(Date)` sem tratamento geraria um Instant completo.
    - `useReceivables.save(request, receivableId?)`: sem `receivableId` cria (`POST`), informado atualiza (`PUT`). O hook não guarda mais nenhum id de rascunho internamente.
    - `usePricingSimulation` (renomeado de `useBatchPricingSimulation`) com `results` como fonte única de verdade dos recebíveis já simulados, atualizado via upsert por `receivableId` e removido via `remove(receivableIds)`.
  - **Fluxo & Regras de Negocio**:
    - Simulação de recebível novo: `POST` no primeiro "Simular", `PUT` no mesmo id nos ajustes seguintes; ao concluir com sucesso, o formulário e limpo e a seção e fechada automaticamente.
    - Simulação em lote dos recebíveis existentes: uma chamada por recebível selecionado, resultados exibidos individualmente, sem soma (decisão explícita, agregação fica para uma iteração futura).
    - Cedente não encontrado pelo CNPJ permite cadastro inline (dialog), reaproveitando o padrão de `FormDialogContainer`.
    - `ReceivableList` agora controla o `ReceivableForm` (visibilidade, estado do form, save) e a seleção da tabela; ao salvar um recebível novo, seleciona-o junto aos já selecionados e simula só ele.
    - Marcar um recebível existente na tabela simula só ele; desmarcar remove só o resultado dele; nenhuma das duas ações reprocessa os demais selecionados.
    - Trocar Data de Referência ou Moeda de Liquidação refaz a simulação de todos os recebíveis que já têm resultado, disparado diretamente nos handlers de mudança dos campos, sem `useEffect`.
  - **Componentes & UX**:
    - Data de Referência da Simulação e Moeda de Liquidação centralizadas em `SimulationParams`, compartilhadas entre a simulação individual e a em lote, evitando duplicar os campos.
    - `usePricingSimulation` passam a ser o único caminho de simulação e exibição de resultado (a simulação individual isolada, que existia separada do lote, foi removida).
    - Formulário de recebível novo escondido por padrão, exposto via botão "Simular Novo Recebível", reduzindo a carga visual inicial da tela.
  - **Alteração no Backend**:
    - o `PrincingResult` estava retornando o campo `term` com o valor em meses, o que poderia causar uma confusão futura, renomeei o campo para `termMonths`.