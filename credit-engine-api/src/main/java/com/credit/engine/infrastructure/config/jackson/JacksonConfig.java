package com.credit.engine.infrastructure.config.jackson;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;

import java.time.Instant;

@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule instantModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Instant.class, new InstantSerializer());
        return module;
    }

}
