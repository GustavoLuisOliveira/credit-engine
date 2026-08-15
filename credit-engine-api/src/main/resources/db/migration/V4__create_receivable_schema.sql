-- =====================================================================
-- V4__create_receivable_schema.sql
-- Contexto: Receivable (Recebível / Título)
-- Ativo financeiro a ser precificado e negociado (Duplicata, Cheque etc.)
-- Depende de: assignor, currency
-- =====================================================================

CREATE TABLE receivable (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignor_id              UUID NOT NULL,
    receivable_type          VARCHAR(50) NOT NULL, -- ex: 'COMMERCIAL_INVOICE', 'POST_DATED_CHECK'
    document_number          VARCHAR(50) NOT NULL, -- número da duplicata/contratos/recebível

    -- Value Object Money (face value do título, moeda original e imutável)
    face_value               NUMERIC(18,4) NOT NULL,
    currency_id              VARCHAR(3)    NOT NULL,

    due_date                 DATE NOT NULL,

    -- Precisa ser escrito na MESMA transação que o settlement_item correspondente,
    -- senão diverge da fonte de verdade (settlement_item.receivable_id).
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

COMMENT ON TABLE receivable IS 'Título a ser precificado; pertence a um único assignor';
COMMENT ON COLUMN receivable.face_value IS 'Valor de face do título na moeda original (Money VO)';
COMMENT ON COLUMN receivable.status IS 'Espelho do estado de liquidação; fonte de verdade é settlement_item';

CREATE TRIGGER trg_receivable_prevent_created_at_update
    BEFORE UPDATE ON receivable
    FOR EACH ROW
    EXECUTE FUNCTION prevent_created_at_update();

CREATE INDEX idx_receivable_assignor ON receivable (assignor_id);
CREATE INDEX idx_receivable_status ON receivable (status);
CREATE INDEX idx_receivable_due_date ON receivable (due_date);