package com.hiby3.pontoapi.model.dto;

public class RegisterRhRequestDTO {

    private Integer empresaId;
    private String email;
    private String password;
    
    // Getters
    public Integer getEmpresaId() { return empresaId; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    // Setters
    public void setEmpresaId(Integer empresaId) { this.empresaId = empresaId; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}