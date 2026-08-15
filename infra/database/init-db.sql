-- Criação do banco de dados com charset UTF-8 e collation en_US.UTF-8
CREATE DATABASE credit_engine_db
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    CONNECTION LIMIT = -1;
   
-- Timezone default do banco (America/Sao_Paulo).
-- Não afeta a integridade dos dados: todas as colunas de data usam TIMESTAMP WITH TIME ZONE, que já armazena em UTC internamente 
-- e é lido como OffsetDateTime no lado Java, então o dado está correto independente desta configuração. 
-- Isso é só conveniência para quem consulta via psql direto (senão os horários aparecem em UTC por padrão).
ALTER DATABASE credit_engine_db SET timezone TO 'America/Sao_Paulo';

