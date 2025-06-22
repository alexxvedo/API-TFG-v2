package com.example.api_v2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación de APIs
 * Proporciona documentación interactiva y especificaciones de la API REST
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .components(createComponents())
                .addSecurityItem(createSecurityRequirement());
    }

    private Info createApiInfo() {
        return new Info()
                .title("Plataforma Colaborativa de Estudio - API")
                .description("""
                    API REST para la plataforma colaborativa de estudio con asistencia de IA.
                    
                    ## Funcionalidades principales:
                    - 👥 **Gestión de usuarios y autenticación**
                    - 🏢 **Workspaces colaborativos**
                    - 📚 **Colecciones de materiales de estudio**
                    - 🃏 **Flashcards con repetición espaciada**
                    - 📄 **Gestión de documentos**
                    - 📝 **Notas colaborativas en tiempo real**
                    - ✅ **Sistema de tareas**
                    - 🤖 **Agente de IA para generación de contenido**
                    - 📊 **Estadísticas y métricas de progreso**
                    
                    ## Autenticación:
                    La API utiliza autenticación JWT. Incluye el token en el header:
                    `Authorization: Bearer <tu-token>`
                    """)
                .version("2.0.0")
                .contact(createContact())
                .license(createLicense());
    }

    private Contact createContact() {
        return new Contact()
                .name("Alejandro Vedo")
                .email("alejandro.vedo@estudiante.usc.es")
                .url("https://github.com/alejandrovedo");
    }

    private License createLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    private List<Server> createServers() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Servidor de desarrollo local"),
                new Server()
                        .url("https://api.plataforma-estudio.com")
                        .description("Servidor de producción")
        );
    }

    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", createSecurityScheme());
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Token JWT para autenticación. Formato: Bearer <token>");
    }

    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement().addList("bearerAuth");
    }
} 