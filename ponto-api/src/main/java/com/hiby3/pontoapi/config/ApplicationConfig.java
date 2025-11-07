package com.hiby3.pontoapi.config;

import com.hiby3.pontoapi.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfig {

    private final UserRepository userRepository;

    public ApplicationConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Este @Bean diz ao Spring Security: "Quando você precisar encontrar um
     * usuário,
     * use este método".
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // Encontra o usuário pelo email
        return username -> userRepository.findByEmail(username);
    }

    /**
     * Este @Bean é necessário para o processo de login.
     * O Spring o usará automaticamente.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Este @Bean define o algoritmo de criptografia de senhas (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}