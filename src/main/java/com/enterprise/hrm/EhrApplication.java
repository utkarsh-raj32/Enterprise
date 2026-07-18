package com.enterprise.hrm;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * ============================================================
 * MAIN APPLICATION ENTRY POINT
 * ============================================================
 *
 * @SpringBootApplication is a meta-annotation that combines:
 *   • @Configuration       — marks this class as a Spring config source
 *   • @EnableAutoConfiguration — triggers Spring Boot's auto-config magic
 *     (sets up DataSource, JPA, Security, Jackson etc. based on classpath)
 *   • @ComponentScan       — scans this package and all sub-packages for
 *     @Component, @Service, @Repository, @Controller etc.
 *
 * WHY use @EnableJpaAuditing?
 *   Our BaseEntity has @CreatedDate and @LastModifiedDate fields.
 *   This annotation activates Spring Data's AuditingEntityListener which
 *   automatically populates those fields on save/update — we never have
 *   to set timestamps manually.
 *
 * WHY use @OpenAPIDefinition?
 *   Injects global metadata (title, version, contact) into the generated
 *   OpenAPI spec shown in Swagger UI.
 */
@SpringBootApplication
@EnableJpaAuditing                           // Required for @CreatedDate / @LastModifiedDate in BaseEntity
@OpenAPIDefinition(
    info = @Info(
        title       = "Enterprise HRM API",
        version     = "v1.0",
        description = "Production-grade REST API for Employee, Leave, Attendance & Salary Management",
        contact     = @Contact(
            name  = "HRM Team",
            email = "hrm@enterprise.com",
            url   = "https://enterprise.com"
        ),
        license     = @License(
            name = "Apache 2.0",
            url  = "https://www.apache.org/licenses/LICENSE-2.0"
        )
    )
)
public class EhrApplication {

    /**
     * Java application entry point.
     * SpringApplication.run():
     *   1. Creates ApplicationContext (IoC container)
     *   2. Registers all beans via component scanning
     *   3. Runs auto-configuration
     *   4. Starts embedded Tomcat on port 8080
     *   5. Fires ApplicationReadyEvent
     */
    public static void main(String[] args) {
        SpringApplication.run(EhrApplication.class, args);
    }
}
