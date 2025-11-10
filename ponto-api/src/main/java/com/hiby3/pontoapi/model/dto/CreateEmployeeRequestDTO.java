package com.hiby3.pontoapi.model.dto;

import java.math.BigDecimal;

public class CreateEmployeeRequestDTO {

    private String name;
    private BigDecimal hourlyRate;
    private String email;
    private String password;

    public String getName() { return name; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}