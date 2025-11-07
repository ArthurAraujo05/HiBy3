package com.hiby3.pontoapi.model.dto;

import java.math.BigDecimal;

// Este é o JSON que o RH vai enviar para criar um novo funcionário
public class CreateEmployeeRequestDTO {

    // Dados para a tabela 'employee' (no banco do cliente)
    private String name;
    private BigDecimal hourlyRate;

    // Dados para a tabela 'users' (no banco 'empresa_master')
    private String email;
    private String password;

    // --- Getters ---
    public String getName() { return name; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}