# Uso de Inteligência Artificial

Este projeto utiliza Inteligência Artificial como ferramenta de apoio
ao desenvolvimento.

## Ferramentas utilizadas

- ChatGPT

## Objetivo do uso

A IA foi utilizada como ferramenta de apoio para:
- discussão de arquitetura;
- análise de requisitos;
- revisão de decisões técnicas;
- geração de documentação;
- identificação de possíveis problemas de design.

## Registro de utilização

## [Feature] Configuração do Banco de Dados PostgreSQL (Docker & Env)
- **Branch**: `feature/postgres-setup`
- **Prompt**: "Definição do serviço PostgreSQL 17 via Docker Compose, suporte a charset UTF-8, parametrização por .env/.env.example e validação do status healthy."
- **Contexto & Decisão**:
  - Escolhida a imagem `postgres:17-alpine` por ser leve, segura e estável.
  - Garantida a inicialização em `UTF8` para suporte completo a caracteres especiais e acentuação nos dados financeiros e cadastrais.
  - Implementado padrão de variáveis de ambiente com `.env.example` versionado e `.env` no `.gitignore`.
  - Configurado `healthcheck` no container para garantir que a base esteja pronta antes dos testes e das migrações da aplicação Spring.
