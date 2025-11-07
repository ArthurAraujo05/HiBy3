package com.hiby3.pontoapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**") // Para todos os endpoints
                        .allowedOrigins("*") // De todas as origens
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS"); // Permite estes métodos
            }
        };
    }
}