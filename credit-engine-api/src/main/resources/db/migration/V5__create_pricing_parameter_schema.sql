-- =====================================================================
-- V5__create_pricing_parameter_schema.sql
-- Contexto: Pricing (Parâmetros de Precificação: Taxa Base & Spread)
-- Fonte de configuração consultada pelo PricingStrategy.
--  =====================================================================

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

COMMENT ON TABLE pricing_parameter IS 'Taxa base e spread vigentes por tipo de título. Append-only: nunca sofre UPDATE, cada mudança de taxa é uma nova linha com nova effective_date preserva histórico completo para auditoria.';
COMMENT ON COLUMN pricing_parameter.effective_date IS 'Data a partir da qual esta taxa passa a valer. A taxa "vigente" numa data X é a de maior effective_date <= X.';

CREATE TRIGGER trg_pricing_parameter_prevent_created_at_update
    BEFORE UPDATE ON pricing_parameter
    FOR EACH ROW
    EXECUTE FUNCTION prevent_created_at_update();

CREATE INDEX idx_pricing_parameter_type_effective_date
    ON pricing_parameter (receivable_type, effective_date DESC);
