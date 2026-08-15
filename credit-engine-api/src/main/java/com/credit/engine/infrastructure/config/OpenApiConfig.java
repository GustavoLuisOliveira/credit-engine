package com.credit.engine.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados exibidos no Swagger UI (/swagger-ui.html) e no schema OpenAPI (/v3/api-docs).
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI creditEngineOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Credit Engine API")
                        .description("Plataforma de cessão de crédito multimoedas. Precificação e liquidação de recebíveis com suporte a múltiplas moedas.")
                        .version("v1")
                        .license(new License().name("Proprietary"))
                );
    }
}

