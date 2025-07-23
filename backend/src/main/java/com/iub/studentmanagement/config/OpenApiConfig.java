package com.iub.studentmanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI documentation.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI documentation with proper titles, descriptions, and server information.
     *
     * @return Configured OpenAPI instance
     */
    @Bean
    public OpenAPI studentManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IUB Student Management API")
                        .description("RESTful API for managing student records at IUB")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("IUB Admin")
                                .email("admin@iub.edu")
                                .url("https://www.iub.edu"))
                        .license(new License()
                                .name("IUB License")
                                .url("https://www.iub.edu/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.iub.edu")
                                .description("Production Server")
                ));
    }
}