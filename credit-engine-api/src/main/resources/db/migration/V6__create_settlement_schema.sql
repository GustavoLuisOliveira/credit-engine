-- =====================================================================
-- V6__create_settlement_schema.sql
-- Contexto: Settlement (Cabeçalho de Liquidação + Item de Liquidação)
-- SettlementItem faz parte do agregado Settlement (não é contexto próprio).
-- Depende de: assignor, receivable, currency
-- =====================================================================

-- ---------------------------------------------------------------------
-- settlement
-- Lote/transação consolidada de cessão realizada na mesa de operação.
-- ---------------------------------------------------------------------
CREATE TABLE settlement (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignor_id             UUID NOT NULL,
    settlement_date_time    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    -- Data-base usada na fórmula de deságio de TODOS os itens do lote (prazo até o vencimento).
    valuation_date          DATE NOT NULL,

    -- Moeda escolhida pelo cedente para receber o valor total do lote
    target_currency_id      VARCHAR(3) NOT NULL,

    -- Totais consolidados (denormalizados a partir dos settlement_items).
    -- Precisam ser escritos na MESMA transação que os itens do lote,
    -- senão divergem da soma real (fonte de verdade são os settlement_items).
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

COMMENT ON TABLE settlement IS 'Cabeçalho de liquidação: lote consolidado de cessão de recebíveis';
COMMENT ON COLUMN settlement.valuation_date IS 'Data-base usada na fórmula de deságio de todos os itens do lote (prazo até o vencimento)';
COMMENT ON COLUMN settlement.target_currency_id IS 'Moeda escolhida pelo cedente para receber o valor total do lote';

CREATE TRIGGER trg_settlement_prevent_created_at_update
    BEFORE UPDATE ON settlement
    FOR EACH ROW
    EXECUTE FUNCTION prevent_created_at_update();

CREATE INDEX idx_settlement_assignor ON settlement (assignor_id);
CREATE INDEX idx_settlement_date ON settlement (settlement_date_time);

-- ---------------------------------------------------------------------
-- settlement_item
-- Fotografia de auditoria imutável do resultado de precificação de cada recebível liquidado.
-- UNIQUE em receivable_id garante a invariante 0..1: um recebível só pode ser liquidado uma única vez.
-- ---------------------------------------------------------------------
CREATE TABLE settlement_item (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    settlement_id            UUID NOT NULL,
    receivable_id            UUID NOT NULL,

    -- Fotografia da regra de precificação (Strategy) aplicada
    -- term e term_months representam o mesmo prazo em duas granularidades:
    -- term é o fato de auditoria legível (dias corridos);
    -- term_months é o valor fracionário exato que efetivamente alimentou o expoente da fórmula de VP
    term                      INT NOT NULL, -- prazo em dias corridos até o vencimento
    term_months               NUMERIC(10,6) NOT NULL, -- prazo fracionário em meses, usado no expoente da fórmula
    base_rate                 NUMERIC(10,6) NOT NULL,
    spread_rate                NUMERIC(10,6) NOT NULL,
    total_rate                 NUMERIC(10,6) GENERATED ALWAYS AS (base_rate + spread_rate) STORED,

    -- Fotografia dos valores originais (moeda do título)
    original_currency_id      VARCHAR(3) NOT NULL,
    face_value                 NUMERIC(18,4) NOT NULL,
    discount_amount            NUMERIC(18,4) NOT NULL,
    present_value               NUMERIC(18,4) NOT NULL,

    -- Fotografia da conversão cambial (1.0000 se same-currency)
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

COMMENT ON TABLE settlement_item IS 'Item de liquidação: fotografia de auditoria imutável de um recebível liquidado';
COMMENT ON COLUMN settlement_item.receivable_id IS 'UNIQUE: garante que um título só é liquidado uma vez (invariante 0..1)';
COMMENT ON COLUMN settlement_item.exchange_rate_used IS '1.0000 quando original_currency_id = settlement_currency_id (same-currency)';
COMMENT ON COLUMN settlement_item.total_rate IS 'Coluna gerada: base_rate + spread_rate, evita divergência entre taxa registrada e taxa aplicada';
COMMENT ON COLUMN settlement_item.term IS 'Prazo em dias corridos até o vencimento, fato de auditoria legível';
COMMENT ON COLUMN settlement_item.term_months IS 'Prazo fracionário em meses efetivamente usado no expoente da fórmula de VP';

CREATE TRIGGER trg_settlement_item_prevent_created_at_update
    BEFORE UPDATE ON settlement_item
    FOR EACH ROW
    EXECUTE FUNCTION prevent_created_at_update();

CREATE INDEX idx_settlement_item_settlement ON settlement_item (settlement_id);