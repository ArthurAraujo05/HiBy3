package com.hiby3.pontoapi.model;

import java.math.BigDecimal;
import java.util.Date;

// DTO (Data Transfer Object)
// para carregar os resultados da nossa view.
public class DailyWorkSummaryDTO {

    private String funcionario;
    private Date data;
    private Double horasTrabalhadas;
    private BigDecimal totalPago;

    // Construtor, Getters e Setters
    public DailyWorkSummaryDTO(String funcionario, Date data, Double horasTrabalhadas, BigDecimal totalPago) {
        this.funcionario = funcionario;
        this.data = data;
        this.horasTrabalhadas = horasTrabalhadas;
        this.totalPago = totalPago;
    }

    // Getters
    public String getFuncionario() { return funcionario; }
    public Date getData() { return data; }
    public Double getHorasTrabalhadas() { return horasTrabalhadas; }
    public BigDecimal getTotalPago() { return totalPago; }
    
    // Setters (são uma boa prática, embora não sejam usados aqui)
}