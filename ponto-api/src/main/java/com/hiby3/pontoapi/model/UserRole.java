package com.hiby3.pontoapi.model;

/**
 * Define a hierarquia (cargos) dos usuários no sistema.
 */
public enum UserRole {
    
    // Nossos três níveis de hierarquia
    ADMIN("ADMIN"),
    RH("RH"),
    FUNCIONARIO("FUNCIONARIO");

    private String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return this.role;
    }
}