package com.example.api_v2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permitir solicitudes desde el frontend en desarrollo
        config.addAllowedOrigin("http://localhost:3000");

        // Permitir todas las solicitudes (GET, POST, DELETE, etc.)
        config.addAllowedMethod("*");

        // Permitir todos los encabezados
        config.addAllowedHeader("*");

        // 🔥 Agregar encabezados expuestos para que el frontend pueda acceder a ellos
        config.addExposedHeader("Content-Disposition");
        config.addExposedHeader("Content-Type");

        // Permitir credenciales (cookies, autenticación)
        config.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

