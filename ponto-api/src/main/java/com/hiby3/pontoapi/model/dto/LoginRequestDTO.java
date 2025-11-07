package com.hiby3.pontoapi.model.dto;

// Esta classe representa o JSON que o frontend vai enviar para /auth/login
public class LoginRequestDTO {

    private String email;
    private String password;

    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}