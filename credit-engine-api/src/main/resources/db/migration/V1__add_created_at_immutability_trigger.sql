-- =====================================================================
-- V1__add_created_at_immutability_trigger.sql
-- Trava created_at contra alteração no nível do banco, independente de quem/o quê está escrevendo (aplicação, script manual, outro client).
-- A proteção via @Column(updatable = false) no JPA só cobre escritas que passam pela aplicação; esta trigger cobre qualquer caminho de escrita.
-- =====================================================================

CREATE OR REPLACE FUNCTION prevent_created_at_update()
RETURNS TRIGGER AS $$
BEGIN
    NEW.created_at := OLD.created_at;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION prevent_created_at_update() IS
    'Ignora qualquer tentativa de alterar created_at em UPDATE, preservando o valor original de INSERT';