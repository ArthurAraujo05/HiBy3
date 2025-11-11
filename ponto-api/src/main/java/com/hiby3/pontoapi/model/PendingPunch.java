package com.hiby3.pontoapi.model;

import java.time.LocalDate;
import java.time.LocalTime;

// DTO que representa um ponto pendente para aprovação do RH
public class PendingPunch {
    
    private Integer punchId;
    private String employeeName;
    private LocalDate date;
    private LocalTime punchIn;
    private LocalTime punchOut; 
    private String status;

    public PendingPunch(Integer punchId, String employeeName, LocalDate date, LocalTime punchIn, LocalTime punchOut, String status) {
        this.punchId = punchId;
        this.employeeName = employeeName;
        this.date = date;
        this.punchIn = punchIn;
        this.punchOut = punchOut;
        this.status = status;
    }

    // Getters (para serialização JSON)

    public Integer getPunchId() { return punchId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getDate() { return date; }
    public LocalTime getPunchIn() { return punchIn; }
    public LocalTime getPunchOut() { return punchOut; }
    public String getStatus() { return status; }
}