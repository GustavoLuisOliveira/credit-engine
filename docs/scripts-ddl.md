# Scripts DDL - SRM Credit Engine

SQL necessário para criar a estrutura completa do banco PostgreSQL 17. Os scripts abaixo correspondem, na ordem, às migrations Flyway do projeto (`V1` a `V7`), aplicadas via Docker Compose.

Ordem de dependência: `currency` precede `assignor`, `receivable` e `settlement`; `assignor` precede `receivable` e `settlement`; `receivable` e `settlement` precedem `settlement_item`.

---

## Pré-requisito. Criação do banco (`init-db.sql`)

Script executado pelo Docker Compose via `docker-entrypoint-initdb.d`, antes de qualquer migration Flyway. Não faz parte da numeração `V1` a `V7`: cria o banco `credit_engine_db` em si, que é pré-requisito para o `V1` rodar.

```sql
-- Criacao do banco de dados com charset UTF-8 e collation en_US.UTF-8
CREATE DATABASE credit_engine_db
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    CONNECTION LIMIT = -1;

-- Timezone default do banco (America/Sao_Paulo).
-- Nao afeta a integridade dos dados: todas as colunas de data usam TIMESTAMP WITH TIME ZONE,
-- que ja armazena em UTC internamente e e lido como OffsetDateTime no lado Java,
-- entao o dado esta correto independente desta configuracao.
-- Isso e so conveniencia para quem consulta via psql direto, senao os horarios aparecem em UTC por padrao.
ALTER DATABASE credit_engine_db SET timezone TO 'America/Sao_Paulo';
```

`LC_COLLATE` e `LC_CTYPE` são fixados na criação do banco e não podem ser alterados depois. A variável `POSTGRES_DB` não deve ser definida no `docker-compose.yml`, para que este script seja o único responsável por criar o banco com o locale correto.

---

## V1. Trigger de imutabilidade de `created_at`

Reutilizada por todas as tabelas abaixo. Garante no nível do banco que `created_at` não é sobrescrito em UPDATE, independente do caminho de escrita (aplicação, script manual, outro client).

```sql
CREATE OR REPLACE FUNCTION prevent_created_at_update()
RETURNS TRIGGER AS $$
BEGIN
    NEW.created_at := OLD.created_at;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION prevent_created_at_update() IS
    'Ignora qualquer tentativa de alterar created_at em UPDATE, preservando o valor original de INSERT';
```

---

## V2. Contexto Currency (Moedas e Câmbio)

```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- currency: moedas operadas pelo fundo (ex: BRL, USD)
-- Chave natural (codigo ISO 4217), tabela de referencia estavel
CREATE TABLE currency (
    id          VARCHAR(3)  PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    symbol      VARCHAR(5)  NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_currency_prevent_created_at_update
    BEFORE UPDATE ON currency
    FOR EACH ROW
    EXECUTE FUNCTION prevent_created_at_update();

COMMENT ON TABLE currency IS 'Moedas suportadas pela plataforma multimoedas';
COMMENT ON COLUMN currency.id IS 'Codigo ISO 4217, usado como chave natural (ex: BRL, USD)';

-- exchange_rate: historico temporal de cotacoes entre pares de moedas
CREATE TABLE exchange_rate (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    origin_currency_id          VARCHAR(3) NOT NULL,
    destination_currency_id     VARCHAR(3) NOT NULL,
    rate                        NUMERIC(18,8) NOT NULL,
    rate_date_time              TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_exchange_rate_origin_currency
        FOREIGN KEY (origin_currency_id) REFERENCES currency (id),
    CONSTRAINT fk_exchange_rate_destination_currency
        FOREIGN KEY (destination_currency_id) REFERENCES currency (id),
    CONSTRAINT chk_exchange_rate_positive_rate
        CHECK (rate > 0),
    CONSTRAINT chk_exchange_rate_distinct_currencies
        CHECK (origin_currency_id <> destination_currency_id)
);

CREATE TRIGGER trg_exchange_rate_prevent_created_at_update
    BEFORE UPDATE ON exchange_rate
    FOR EACH ROW
    EXECUTE FUNCTION prevent_created_at_update();

COMMENT ON TABLE exchange_rate IS 'Historico de cotacoes cambiais entre pares de moedas, com timestamp exato';
COMMENT ON COLUMN exchange_rate.rate IS '1 unidade da moeda origem equivale a rate unidades da moeda destino';

CREATE INDEX idx_exchange_rate_pair_date
    ON exchange_rate (origin_currency_id, destination_currency_id, rate_date_time DESC);
```

---

## V3. Contexto Assignor (Cedente)

Regra de negócio: o Credit Engine opera exclusivamente com cedentes Pessoa Jurídica. Apenas CNPJ é aceito.

```sql
CREATE TABLE assignor (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_number  VARCHAR(14)  NOT NULL,
    name             VARCHAR(100) NOT NULL,
    email            VARCHAR(100) NOT NULL,
    phone            VARCHAR(20),
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT uq_assignor_document_number UNIQUE (document_number),
    CONSTRAINT ck_assignor_document_number_cnpj CHECK (document_number ~ '^\d{14}$')
);

COMMENT ON TABLE assignor IS 'Cedentes: empresas (Pessoa Juridica) detentoras dos recebiveis, 1:N com receivable';
COMMENT ON COLUMN assignor.document_number IS 'CNPJ do cedente (14 digitos numericos, sem mascara)';

CREATE TRIGGER trg_assignor_prevent_created_at_update
    BEFORE UPDATE ON assignor
    FOR EACH ROW
    EXECUTE FUNCTION prevent_created_at_update();

CREATE INDEX idx_assignor_document_number ON assignor (document_number);
```

---

## V4. Contexto Receivable (Recebível / Título)

Depende de `assignor` e `currency`.

```sql
CREATE TABLE receivable (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignor_id              UUID NOT NULL,
    receivable_type          VARCHAR(50) NOT NULL,
    document_number          VARCHAR(50) NOT NULL,

    face_value               NUMERIC(18,4) NOT NULL,
    currency_id              VARCHAR(3)    NOT NULL,

    due_date                 DATE NOT NULL,

    status                    VARCHAR(20) NOT NULL DEFAULT 'UNSETTLED',

    created_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_receivable_assignor
        FOREIGN KEY (assignor_id) REFERENCES assignor (id),
    CONSTRAINT fk_receivable_currency
        FOREIGN KEY (currency_id) REFERENCES currency (id),
    CONSTRAINT chk_receivable_face_value_positive
        CHECK (face_value > 0),
    CONSTRAINT chk_receivable_status
        CHECK (status IN ('UNSETTLED', 'SETTLED', 'CANCELLED'))
);

COMMENT ON TABLE receivable IS 'Titulo a ser precificado, pertence a um unico assignor';
COMMENT ON COLUMN receivable.face_value IS 'Valor de face do titulo na moeda original (Money VO)';
COMMENT ON COLUMN receivable.status IS 'Espelho do estado de liquidacao, fonte de verdade e settlement_item';

CREATE TRIGGER trg_receivable_prevent_created_at_update
    BEFORE UPDATE ON receivable
    FOR EACH ROW
    EXECUTE FUNCTION prevent_created_at_update();

CREATE INDEX idx_receivable_assignor ON receivable (assignor_id);
CREATE INDEX idx_receivable_status ON receivable (status);
CREATE INDEX idx_receivable_due_date ON receivable (due_date);
```

---

## V5. Contexto Pricing (Parâmetros de Precificação)

Fonte de configuração consultada pelo `PricingStrategy`. Append-only: nunca sofre UPDATE de taxa, cada mudança gera nova linha.

```sql
CREATE TABLE pricing_parameter (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receivable_type   VARCHAR(50) NOT NULL,
    base_rate         NUMERIC(9,6) NOT NULL,
    spread_rate       NUMERIC(9,6) NOT NULL,
    effective_date    DATE NOT NULL,

    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT chk_pricing_parameter_type
        CHECK (receivable_type IN ('COMMERCIAL_INVOICE', 'POST_DATED_CHECK')),
    CONSTRAINT chk_pricing_parameter_base_rate_non_negative
        CHECK (base_rate >= 0),
    CONSTRAINT chk_pricing_parameter_spread_rate_non_negative
        CHECK (spread_rate >= 0)
);

COMMENT ON TABLE pricing_parameter IS 'Taxa base e spread vigentes por tipo de titulo. Append-only, nunca sofre UPDATE, cada mudanca de taxa e uma nova linha com nova effective_date. Preserva historico completo para auditoria.';
COMMENT ON COLUMN pricing_parameter.effective_date IS 'Data a partir da qual esta taxa passa a valer. A taxa vigente numa data X e a de maior effective_date menor ou igual a X.';

CREATE TRIGGER trg_pricing_parameter_prevent_created_at_update
    BEFORE UPDATE ON pricing_parameter
    FOR EACH ROW
    EXECUTE FUNCTION prevent_created_at_update();

CREATE INDEX idx_pricing_parameter_type_effective_date
    ON pricing_parameter (receivable_type, effective_date DESC);
```

---

## V6. Contexto Settlement (Cabeçalho + Item de Liquidação)

`settlement_item` faz parte do agregado `Settlement`, não é contexto próprio. Depende de `assignor`, `receivable` e `currency`.

```sql
-- settlement: lote/transacao consolidada de cessao realizada na mesa de operacao
CREATE TABLE settlement (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignor_id             UUID NOT NULL,
    settlement_date_time    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    valuation_date          DATE NOT NULL,

    target_currency_id      VARCHAR(3) NOT NULL,

    total_face_value        NUMERIC(18,4) NOT NULL,
    total_discount_amount   NUMERIC(18,4) NOT NULL,
    total_net_amount        NUMERIC(18,4) NOT NULL,

    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_settlement_assignor
        FOREIGN KEY (assignor_id) REFERENCES assignor (id),
    CONSTRAINT fk_settlement_target_currency
        FOREIGN KEY (target_currency_id) REFERENCES currency (id),
    CONSTRAINT chk_settlement_totals_non_negative
        CHECK (total_face_value >= 0 AND total_discount_amount >= 0 AND total_net_amount >= 0)
);

COMMENT ON TABLE settlement IS 'Cabecalho de liquidacao: lote consolidado de cessao de recebiveis';
COMMENT ON COLUMN settlement.valuation_date IS 'Data-base usada na formula de desagio de todos os itens do lote (prazo ate o vencimento)';
COMMENT ON COLUMN settlement.target_currency_id IS 'Moeda escolhida pelo cedente para receber o valor total do lote';

CREATE TRIGGER trg_settlement_prevent_created_at_update
    BEFORE UPDATE ON settlement
    FOR EACH ROW
    EXECUTE FUNCTION prevent_created_at_update();

CREATE INDEX idx_settlement_assignor ON settlement (assignor_id);
CREATE INDEX idx_settlement_date ON settlement (settlement_date_time);

-- settlement_item: fotografia de auditoria imutavel do resultado de precificacao de cada recebivel liquidado
-- UNIQUE em receivable_id garante a invariante 0..1: um recebivel so pode ser liquidado uma unica vez
CREATE TABLE settlement_item (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    settlement_id            UUID NOT NULL,
    receivable_id            UUID NOT NULL,

    term                      INT NOT NULL,
    term_months               NUMERIC(10,6) NOT NULL,
    base_rate                 NUMERIC(10,6) NOT NULL,
    spread_rate                NUMERIC(10,6) NOT NULL,
    total_rate                 NUMERIC(10,6) GENERATED ALWAYS AS (base_rate + spread_rate) STORED,

    original_currency_id      VARCHAR(3) NOT NULL,
    face_value                 NUMERIC(18,4) NOT NULL,
    discount_amount            NUMERIC(18,4) NOT NULL,
    present_value               NUMERIC(18,4) NOT NULL,

    settlement_currency_id     VARCHAR(3) NOT NULL,
    exchange_rate_used         NUMERIC(18,8) NOT NULL,
    settlement_amount          NUMERIC(18,4) NOT NULL,

    created_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT uq_settlement_item_receivable UNIQUE (receivable_id),
    CONSTRAINT fk_settlement_item_settlement
        FOREIGN KEY (settlement_id) REFERENCES settlement (id) ON DELETE CASCADE,
    CONSTRAINT fk_settlement_item_receivable
        FOREIGN KEY (receivable_id) REFERENCES receivable (id),
    CONSTRAINT fk_settlement_item_original_currency
        FOREIGN KEY (original_currency_id) REFERENCES currency (id),
    CONSTRAINT fk_settlement_item_settlement_currency
        FOREIGN KEY (settlement_currency_id) REFERENCES currency (id),
    CONSTRAINT chk_settlement_item_term_positive
        CHECK (term > 0),
    CONSTRAINT chk_settlement_item_term_months_positive
        CHECK (term_months > 0),
    CONSTRAINT chk_settlement_item_rates_non_negative
        CHECK (base_rate >= 0 AND spread_rate >= 0),
    CONSTRAINT chk_settlement_item_values_positive
        CHECK (face_value > 0 AND present_value > 0 AND settlement_amount > 0),
    CONSTRAINT chk_settlement_item_discount_non_negative
        CHECK (discount_amount >= 0),
    CONSTRAINT chk_settlement_item_exchange_rate_positive
        CHECK (exchange_rate_used > 0)
);

COMMENT ON TABLE settlement_item IS 'Item de liquidacao: fotografia de auditoria imutavel de um recebivel liquidado';
COMMENT ON COLUMN settlement_item.receivable_id IS 'UNIQUE, garante que um titulo so e liquidado uma vez (invariante 0..1)';
COMMENT ON COLUMN settlement_item.exchange_rate_used IS '1.0000 quando original_currency_id igual a settlement_currency_id (same-currency)';
COMMENT ON COLUMN settlement_item.total_rate IS 'Coluna gerada: base_rate + spread_rate, evita divergencia entre taxa registrada e taxa aplicada';
COMMENT ON COLUMN settlement_item.term IS 'Prazo em dias corridos ate o vencimento, fato de auditoria legivel';
COMMENT ON COLUMN settlement_item.term_months IS 'Prazo fracionario em meses efetivamente usado no expoente da formula de VP';

CREATE TRIGGER trg_settlement_item_prevent_created_at_update
    BEFORE UPDATE ON settlement_item
    FOR EACH ROW
    EXECUTE FUNCTION prevent_created_at_update();

CREATE INDEX idx_settlement_item_settlement ON settlement_item (settlement_id);
```

---

## V7. Índices de suporte ao Extrato de Liquidação

Suporte às consultas analíticas por período e por cedente + período, exigidas na rota de Extrato de Liquidação.

```sql
-- Cobre a consulta de extrato filtrada apenas por periodo (sem cedente)
CREATE INDEX idx_settlement_valuation_date
    ON settlement (valuation_date DESC);

-- Cobre a consulta de extrato filtrada por cedente e periodo, o combo
-- mais comum de uso (mesa de operacao consultando o historico de um
-- cedente especifico num intervalo de datas)
CREATE INDEX idx_settlement_assignor_valuation_date
    ON settlement (assignor_id, valuation_date DESC);

COMMENT ON INDEX idx_settlement_valuation_date IS 'Suporte ao filtro de periodo do Extrato de Liquidacao';
COMMENT ON INDEX idx_settlement_assignor_valuation_date IS 'Suporte ao filtro combinado de cedente e periodo do Extrato de Liquidacao';
```

---

## Resumo das tabelas

| Tabela | Contexto | Chave primária | Append-only / imutável |
|---|---|---|---|
| `currency` | Currency | `id` (código ISO 4217) | Não |
| `exchange_rate` | Currency | UUID | Sim, histórico |
| `assignor` | Assignor | UUID | Não |
| `receivable` | Receivable | UUID | Não (`status` muda) |
| `pricing_parameter` | Pricing | UUID | Sim, nova linha por mudança de taxa |
| `settlement` | Settlement | UUID | Sim, totais gravados na criação |
| `settlement_item` | Settlement | UUID | Sim, fotografia de auditoria |

Todas as tabelas usam a trigger `prevent_created_at_update` (V1) para impedir alteração de `created_at` no nível do banco.

O banco `credit_engine_db` em si é criado pelo `init-db.sql` (pré-requisito, fora da numeração Flyway), com locale `en_US.utf8` e timezone padrão `America/Sao_Paulo`.
