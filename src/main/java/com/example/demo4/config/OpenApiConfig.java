package com.example.demo4.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        private static final String BEARER_SCHEME = "bearerAuth";
        private static final String API_TITLE = "Demo4 API";
        private static final String API_DESCRIPTION = "API documentation for Demo4 backend";
        private static final String API_VERSION = "v1.0.0";

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(apiInfo())
                                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                                .components(new Components().addSecuritySchemes(BEARER_SCHEME, bearerScheme()));
        }

        private Info apiInfo() {
                return new Info()
                                .title(API_TITLE)
                                .description(API_DESCRIPTION)
                                .version(API_VERSION);
        }

        private SecurityScheme bearerScheme() {
                return new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT");
        }
}