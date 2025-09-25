package com.avivse.storeservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI storeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Store API")
                        .description("API for managing store information and operations")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Aviv")
                                .email("aviv@example.com")));
    }

    @Bean
    ModelResolver modelResolver(final ObjectMapper objectMapper) {
        return new ModelResolver(objectMapper);
    }
}