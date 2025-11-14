package com.hiby3.pontoapi.service;

import com.hiby3.pontoapi.model.User; // <-- IMPORT ADICIONADO
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
// import java.sql.Date; // <-- IMPORT REMOVIDO (CAUSA DO ERRO)
import java.util.Date; // <-- IMPORT CORRETO ADICIONADO
import java.time.ZoneOffset; // <-- IMPORT ADICIONADO (para conversão de data)
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${jwt.expiration-ms}")
    private long JWT_EXPIRATION_MS;

    /**
     * Extrai o username (nosso email) de um token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Gera um token de login para um usuário.
     * ESTE MÉTODO FOI MODIFICADO PARA INCLUIR OS CLAIMS DE LICENÇA.
     */
    public String generateToken(UserDetails userDetails) {
        // 1. Criar o mapa de claims
        Map<String, Object> extraClaims = new HashMap<>();

        // 2. Verificar se é o nosso objeto User e adicionar claims de licença
        // 
        if (userDetails instanceof User user) {
            
            // Adiciona o Tier da Licença (TRIAL, PROFESSIONAL, etc.)
            if (user.getLicencaTier() != null) {
                extraClaims.put("tier", user.getLicencaTier());
            }

            // Adiciona a Data de Expiração do Teste (se for TRIAL)
            if (user.getTrialEndDate() != null) {
                // Converte LocalDate para long (epoch millis) para o JWT
                // MUDANÇA: Substitui Date.valueOf() pela conversão correta de LocalDate
                long trialEndMillis = user.getTrialEndDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
                extraClaims.put("trial_end", trialEndMillis);
            }

            // Adiciona o Status da Conta (ATIVO, TESTE_EXPIRADO)
            if (user.getStatus() != null) {
                extraClaims.put("status", user.getStatus());
            }

            // Adiciona o ID da Empresa (TenantId) para simplificar consultas
            if (user.getEmpresa() != null) {
                extraClaims.put("tenant_id", user.getEmpresa().getId());
            }
        }

        // 3. Chamar o método principal com os claims
        return generateToken(extraClaims, userDetails);
    }

    /**
     * Gera um token de login com "claims"
     * (Este método não muda)
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS)) // <-- Usa a variável
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Verifica se um token é válido
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // --- Métodos Privados Auxiliares (agora não-estáticos) ---
    // (Corrigidos para usar java.util.Date)

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date()); // <-- CORRIGIDO (Usa java.util.Date)
    }

    private Date extractExpiration(String token) { // <-- CORRIGIDO (Usa java.util.Date)
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}