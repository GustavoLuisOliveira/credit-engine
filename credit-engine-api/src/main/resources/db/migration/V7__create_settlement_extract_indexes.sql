-- =====================================================================
-- V7__create_settlement_extract_indexes.sql
-- Suporte a Consultas Analiticas (Extrato de Liquidacao).
-- O filtro de periodo do extrato usa valuation_date, que ate aqui nao
-- tinha indice proprio (settlement so tinha indice em assignor_id e em
-- settlement_date_time, que e outro campo).
-- =====================================================================

-- Cobre a consulta de extrato filtrada apenas por periodo (sem cedente).
CREATE INDEX idx_settlement_valuation_date
    ON settlement (valuation_date DESC);

-- Cobre a consulta de extrato filtrada por cedente e periodo, o combo
-- mais comum de uso (mesa de operacao consultando o historico de um
-- cedente especifico num intervalo de datas).
CREATE INDEX idx_settlement_assignor_valuation_date
    ON settlement (assignor_id, valuation_date DESC);

COMMENT ON INDEX idx_settlement_valuation_date IS 'Suporte ao filtro de periodo do Extrato de Liquidacao';
COMMENT ON INDEX idx_settlement_assignor_valuation_date IS 'Suporte ao filtro combinado de cedente e periodo do Extrato de Liquidacao';
