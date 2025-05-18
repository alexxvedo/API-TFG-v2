package com.example.api_v2.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Registrar información de depuración sobre la solicitud
            log.info("Procesando solicitud: {} {}", request.getMethod(), request.getRequestURI());
            log.info("Headers de la solicitud:");
            Collections.list(request.getHeaderNames()).forEach(headerName -> 
                log.info("  {}: {}", headerName, request.getHeader(headerName))
            );
            
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                log.info("Token encontrado: {}", token);
                
                // En un sistema real, aquí verificaríamos el token JWT
                // Por ahora, simplemente extraemos el email del token (simulado)
                String email = extractEmailFromToken(token);
                
                if (email != null) {
                    // Crear un objeto Authentication y establecerlo en el SecurityContext
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            email, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Usuario autenticado: {}", email);
                }
            } else {
                // Intentar autenticar usando un parámetro de consulta (solo para depuración)
                String emailParam = request.getParameter("email");
                if (emailParam != null && !emailParam.isEmpty()) {
                    log.info("Autenticando usando parámetro de email: {}", emailParam);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            emailParam, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.info("No se encontró token de autenticación ni parámetro de email");
                }
            }
        } catch (Exception e) {
            log.error("Error al procesar la autenticación", e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractEmailFromToken(String token) {
        // En un sistema real, aquí decodificaríamos el token JWT
        // Por ahora, simplemente devolvemos el token como si fuera el email
        return token;
    }
}
