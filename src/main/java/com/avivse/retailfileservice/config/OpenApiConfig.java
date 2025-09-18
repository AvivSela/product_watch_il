package com.avivse.retailfileservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI retailFileServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Retail File API")
                        .description("API for managing file uploads and processing for retail chains and stores")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Aviv")
                                .email("aviv@example.com")));
    }
}