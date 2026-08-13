-- Criação do banco de dados com charset UTF-8 e collation pt_BR.UTF-8
CREATE DATABASE credit_engine_db
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'pt_BR.UTF-8'
    LC_CTYPE = 'pt_BR.UTF-8'
    CONNECTION LIMIT = -1;