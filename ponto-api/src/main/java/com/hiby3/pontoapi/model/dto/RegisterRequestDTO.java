package com.hiby3.pontoapi.model.dto;

import com.hiby3.pontoapi.model.UserRole;

public class RegisterRequestDTO {
    
    private String email;
    private String password;
    private UserRole role; // O Frontend deve enviar ROLE_RH para o Trial
    private String planTier; // TRIAL, PROFESSIONAL
    
    // NOVO CAMPO: O nome da empresa para criar o Tenant
    private String companyName;

    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }
    public String getPlanTier() { return planTier; }
    public String getCompanyName() { return companyName; } // <-- GETTER CORRETO

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(UserRole role) { this.role = role; }
    public void setPlanTier(String planTier) { this.planTier = planTier; }
    
    // SETTER CORRIGIDO (estava getCompanyName por engano)
    public void setCompanyName(String companyName) { 
        this.companyName = companyName; 
    }
}