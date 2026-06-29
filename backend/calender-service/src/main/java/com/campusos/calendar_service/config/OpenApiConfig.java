package com.campusos.calendar_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI config. Adds the "Authorize" (Bearer JWT) button and points
 * "Try it out" at the API gateway, so requests are validated + get the X-Auth-*
 * identity headers injected (required for protected endpoints).
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.gateway-url:http://localhost:8080}")
    private String gatewayUrl;

    @Bean
    public OpenAPI openAPI() {
        final String scheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("Calendar Service API").version("v1")
                        .description("CampusOS holidays & calendar"))
                .servers(List.of(new Server().url(gatewayUrl).description("API Gateway")))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(scheme,
                        new SecurityScheme()
                                .name(scheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
