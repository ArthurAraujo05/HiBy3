package com.hiby3.pontoapi.model.dto;

// Esta classe representa o JSON que o backend vai devolver
// após um login ou registro bem-sucedido.
public class AuthenticationResponseDTO {
    
    private String token;

    public AuthenticationResponseDTO(String token) {
        this.token = token;
    }

    // Getter
    public String getToken() { return token; }

    // Setter
    public void setToken(String token) { this.token = token; }
}