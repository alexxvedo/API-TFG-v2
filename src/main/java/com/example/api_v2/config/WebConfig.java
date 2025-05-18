package com.example.api_v2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;

@Configuration
public class WebConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permitir solicitudes desde el frontend en desarrollo
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Permitir todas las solicitudes (GET, POST, DELETE, etc.)
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));

        // Permitir todos los encabezados
        config.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "Accept", "Origin", "X-Requested-With"));

        // Agregar encabezados expuestos para que el frontend pueda acceder a ellos
        config.setExposedHeaders(Arrays.asList("Content-Disposition", "Content-Type", "Authorization"));

        // Permitir credenciales (cookies, autenticación)
        config.setAllowCredentials(true);
        
        // Tiempo máximo de caché para la respuesta pre-vuelo (preflight)
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
