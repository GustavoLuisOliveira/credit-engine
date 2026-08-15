-- =====================================================================
-- V3__create_assignor_schema.sql
-- Contexto: Assignor (Cedente)
-- Empresa detentora original dos títulos que busca liquidez.
--
-- Regra de negócio: o Credit Engine opera exclusivamente com cedentes Pessoa Jurídica.
-- Apenas CNPJ é aceito como documento (CPF não é suportado neste domínio).
-- =====================================================================

CREATE TABLE assignor (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_number  VARCHAR(14)  NOT NULL, -- CNPJ (apenas dígitos, sem máscara)
    name             VARCHAR(100) NOT NULL,
    email            VARCHAR(100) NOT NULL,
    phone            VARCHAR(20),
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT uq_assignor_document_number UNIQUE (document_number),
    CONSTRAINT ck_assignor_document_number_cnpj CHECK (document_number ~ '^\d{14}$')
);

COMMENT ON TABLE assignor IS 'Cedentes: empresas (Pessoa Jurídica) detentoras dos recebíveis (1:N com receivable)';
COMMENT ON COLUMN assignor.document_number IS 'CNPJ do cedente (14 dígitos numéricos, sem máscara)';

CREATE TRIGGER trg_assignor_prevent_created_at_update
    BEFORE UPDATE ON assignor
    FOR EACH ROW
    EXECUTE FUNCTION prevent_created_at_update();

CREATE INDEX idx_assignor_document_number ON assignor (document_number);