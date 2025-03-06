package com.example.api_v2.initializers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ExtensionInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // Asegúrate de que el usuario de la base de datos tenga permisos para crear extensiones
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector;");
    }
}
