package com.example.HCL_FInal.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI foodOrderingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Food Ordering System API")
                        .description("REST API for the Food Ordering System with Admin, Manager and User roles. "
                                + "Use the /auth/login endpoint to obtain a JWT, then click Authorize and paste the token.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Food Ordering Team")
                                .email("support@foodorder.com"))
                        .license(new License().name("Educational Use")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste only the token; the Bearer prefix is added automatically")));
    }
}
