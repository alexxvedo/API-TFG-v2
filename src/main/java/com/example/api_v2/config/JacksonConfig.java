    package com.example.api_v2.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configurar para ignorar campos desconocidos por defecto
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Registrar el módulo Hibernate5Module para manejar los proxies de Hibernate
        Hibernate5JakartaModule hibernateModule = new Hibernate5JakartaModule();
        // Configurar para que no falle en propiedades lazy no inicializadas
        hibernateModule.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, false);
        hibernateModule.configure(Hibernate5JakartaModule.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);
        
        // Registrar el módulo para manejar tipos de fecha/hora de Java 8
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        mapper.registerModules(hibernateModule, javaTimeModule);
        
        return mapper;
    }
} 