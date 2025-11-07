package com.hiby3.pontoapi.config;

import com.hiby3.pontoapi.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Diz ao Spring para gerenciar esta classe
public class JwtAuthenticationFilter extends OncePerRequestFilter { // Garante que o filtro rode só UMA VEZ por requisição

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // O @Bean que criamos no ApplicationConfig

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Pega o "cabeçalho" da requisição onde o token deve estar
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Se não tem cabeçalho, ou se não começa com "Bearer ", é uma requisição pública.
        // Deixa ela passar para o próximo filtro (que pode ser o filtro de "página de login")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrai o token (ex: "Bearer <token-aqui>" -> "<token-aqui>")
        jwt = authHeader.substring(7);

        // 4. Pergunta ao JwtService: "Quem é o dono desse token?"
        userEmail = jwtService.extractUsername(jwt);

        // 5. Se temos um email E o usuário ainda não foi autenticado nesta requisição...
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 6. ...busca o usuário no banco de dados (usando nosso UserRepository)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 7. Se o token for válido (não expirou e a assinatura bate)...
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                // 8. ...AUTENTICA O USUÁRIO para esta requisição!
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Não precisamos de credenciais (senha) pois estamos usando token
                        userDetails.getAuthorities() // As "Roles" (hierarquia)
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // 9. Coloca o usuário autenticado no "Contexto de Segurança"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 10. Passa a requisição (agora autenticada) para o próximo filtro
        filterChain.doFilter(request, response);
    }
}