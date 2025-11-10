package com.hiby3.pontoapi.model.dto;

import java.sql.Timestamp;

public class PendingPunchDTO {

    // Dados da batida
    private Integer punchId;
    private Timestamp originalTimestamp;
    private String eventType;
    
    // Dados da Edição
    private Timestamp requestedTimestamp;
    private String reason;
    
    // Dados do Funcionário
    private Integer employeeId;
    private String employeeName;

    // Construtor
    public PendingPunchDTO(Integer punchId, Timestamp originalTimestamp, String eventType, 
                           Timestamp requestedTimestamp, String reason, 
                           Integer employeeId, String employeeName) {
        this.punchId = punchId;
        this.originalTimestamp = originalTimestamp;
        this.eventType = eventType;
        this.requestedTimestamp = requestedTimestamp;
        this.reason = reason;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
    }

    // Getters (O Jackson usa para serializar o JSON)
    public Integer getPunchId() { return punchId; }
    public Timestamp getOriginalTimestamp() { return originalTimestamp; }
    public String getEventType() { return eventType; }
    public Timestamp getRequestedTimestamp() { return requestedTimestamp; }
    public String getReason() { return reason; }
    public Integer getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
}