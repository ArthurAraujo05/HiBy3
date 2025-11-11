package com.hiby3.pontoapi.model;

import java.math.BigDecimal;

// Este é um simples DTO/Modelo para a tabela 'employee'
public class Employee {
    
    private Integer id;
    private String name;
    private BigDecimal hourlyRate;

    public Employee(Integer id, String name, BigDecimal hourlyRate) {
        this.id = id;
        this.name = name;
        this.hourlyRate = hourlyRate;
    }

    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    
    // Setters (Não são necessários para a listagem, mas boa prática)
    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
}