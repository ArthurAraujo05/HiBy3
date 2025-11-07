package com.hiby3.pontoapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Importante
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper; // <-- O "tradutor" de JSON
import com.hiby3.pontoapi.model.dto.ErrorResponseDTO; // <-- Nosso DTO de erro
import jakarta.servlet.http.HttpServletResponse; // <-- Para escrever a resposta
import org.springframework.http.MediaType; // <-- Para dizer que é JSON
import org.springframework.security.web.access.AccessDeniedHandler; // <-- O "plug"

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    // O AuthenticationProvider foi removido daqui

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // --- ESTE É O BLOCO NOVO ---
                // Diz ao Spring para usar NOSSOS tratadores de erro, e não os dele
                .exceptionHandling(exceptionHandling -> exceptionHandling.accessDeniedHandler(accessDeniedHandler()) // Nosso
                                                                                                                     // tratador
                                                                                                                     // para
                                                                                                                     // 403
                )
                // --- FIM DO BLOCO NOVO ---

                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos
                        .requestMatchers("/auth/**").permitAll()

                        // Regra do ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Regras do RH
                        .requestMatchers("/api/rh/**").hasAnyRole("RH", "ADMIN")
                        .requestMatchers("/api/reports/**").hasAnyRole("RH", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/employees").hasAnyRole("RH", "ADMIN")

                        // Regras do Funcionário (e RH/Admin)
                        .requestMatchers("/api/empresas").authenticated()
                        .requestMatchers("/api/punches/**").authenticated()

                        // Todo o resto
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Este é o @Bean que cria o nosso "Tratador de Acesso Negado (403)"
     * Ele vai manualmente criar e escrever o JSON de erro.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {

            ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                    HttpStatus.FORBIDDEN.value(),
                    "Forbidden",
                    "Acesso Negado. Você não tem permissão para executar esta ação.",
                    request.getRequestURI());

            // Escreve o JSON de erro manualmente na resposta
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
        };
    }
}