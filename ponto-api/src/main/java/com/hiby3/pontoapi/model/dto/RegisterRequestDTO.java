package com.hiby3.pontoapi.model.dto;

import com.hiby3.pontoapi.model.UserRole;

// Esta classe representa o JSON que o frontend vai enviar para /auth/register
public class RegisterRequestDTO {
    
    private String email;
    private String password;
    private UserRole role; // A "hierarquia" (ADMIN, RH, ou FUNCIONARIO)

    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(UserRole role) { this.role = role; }
}