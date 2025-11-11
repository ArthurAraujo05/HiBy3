package com.hiby3.pontoapi.model;

import java.sql.Timestamp;

// Representa um único evento na tabela 'punch'
public class PunchRecord {
    
    private Integer id;
    private Timestamp timestamp;
    private String eventType;
    private String status;

    public PunchRecord(Integer id, Timestamp timestamp, String eventType, String status) {
        this.id = id;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.status = status;
    }

    // Getters
    public Integer getId() { return id; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getEventType() { return eventType; }
    public String getStatus() { return status; }
}