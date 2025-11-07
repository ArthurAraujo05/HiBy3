package com.hiby3.pontoapi.controller;

import com.hiby3.pontoapi.model.dto.AuthenticationResponseDTO;
import com.hiby3.pontoapi.model.dto.LoginRequestDTO;
import com.hiby3.pontoapi.model.dto.RegisterRequestDTO;
import com.hiby3.pontoapi.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth") // URL base /auth (que definimos como pública no SecurityConfiguration)
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Endpoint para REGISTRAR um novo usuário.
     * URL: POST http://localhost:8080/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(
            @RequestBody RegisterRequestDTO request
    ) {
        // @RequestBody pega o JSON enviado pelo frontend e transforma no DTO
        
        // Chama o nosso "cérebro" para registrar e gerar o token
        AuthenticationResponseDTO response = authenticationService.register(request);
        
        // Retorna o token com um status HTTP 200 (OK)
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para LOGAR (autenticar) um usuário.
     * URL: POST http://localhost:8080/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(
            @RequestBody LoginRequestDTO request
    ) {
        // Chama o nosso "cérebro" para logar e gerar o token
        AuthenticationResponseDTO response = authenticationService.login(request);
        
        // Retorna o token com um status HTTP 200 (OK)
        return ResponseEntity.ok(response);
    }
}