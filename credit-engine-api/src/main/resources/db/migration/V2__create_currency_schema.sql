-- =====================================================================
-- V2__create_currency_schema.sql
-- Contexto: Currency (Gestão de Câmbio)
-- Cria a base de moedas suportadas e o histórico de cotações cambiais.
-- Pré-requisito para os contextos Receivable e Settlement (FKs futuras).
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ---------------------------------------------------------------------
-- currency
-- Moedas operadas pelo fundo (ex: BRL, USD).
-- Chave natural (código ISO 4217): tabela de referência estável, não segue o BaseEntity/UUID padrão das demais entidades transacionais.
-- ---------------------------------------------------------------------
CREATE TABLE currency (
    id          VARCHAR(3)  PRIMARY KEY, -- código ISO 4217, ex: 'BRL', 'USD'
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
COMMENT ON COLUMN currency.id IS 'Código ISO 4217, usado como chave natural (ex: BRL, USD)';

-- ---------------------------------------------------------------------
-- exchange_rate
-- Histórico temporal de cotações entre pares de moedas.
-- ---------------------------------------------------------------------
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

COMMENT ON TABLE exchange_rate IS 'Histórico de cotações cambiais entre pares de moedas, com timestamp exato';
COMMENT ON COLUMN exchange_rate.rate IS 'Taxa de conversão: 1 unidade da moeda origem = rate unidades da moeda destino';

-- Otimiza a busca pela cotação mais recente de um par de moedas
CREATE INDEX idx_exchange_rate_pair_date
    ON exchange_rate (origin_currency_id, destination_currency_id, rate_date_time DESC);