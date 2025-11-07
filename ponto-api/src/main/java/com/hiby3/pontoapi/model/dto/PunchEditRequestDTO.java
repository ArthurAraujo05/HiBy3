package com.hiby3.pontoapi.model.dto;

// Este é o JSON que o funcionário vai enviar ao solicitar
// uma alteração de ponto.
public class PunchEditRequestDTO {

    private String requestedTimestamp; // O novo horário (ex: "2025-11-07 09:01:00")
    private String reason; // O motivo (ex: "Esqueci de bater o ponto")

    // Getters e Setters
    public String getRequestedTimestamp() {
        return requestedTimestamp;
    }

    public void setRequestedTimestamp(String requestedTimestamp) {
        this.requestedTimestamp = requestedTimestamp;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}