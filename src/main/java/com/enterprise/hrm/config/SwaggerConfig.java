package com.enterprise.hrm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================
 * SWAGGER / OPENAPI 3 CONFIGURATION
 * ============================================================
 *
 * springdoc-openapi auto-generates the OpenAPI spec from
 * Spring MVC annotations (@RestController, @GetMapping etc.)
 * This config adds JWT Bearer token support to Swagger UI
 * so testers can authenticate and try out protected endpoints.
 *
 * @Bean OpenAPI:
 *   Customizes the global OpenAPI spec object.
 *   We add a "Bearer Authentication" security scheme that
 *   adds an "Authorization: Bearer <token>" header to requests.
 *
 * After login, testers copy the JWT and click "Authorize" in
 * Swagger UI to use it for all subsequent requests.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // Add security requirement globally — all endpoints
                // show the lock icon in Swagger UI
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                // Define the bearer token security scheme
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer token (without 'Bearer ' prefix)")
                        )
                );
    }
}
