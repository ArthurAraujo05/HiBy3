package com.hiby3.pontoapi.model.dto;


// uma alteração de ponto.
public class PunchEditRequestDTO {

    private String requestedTimestamp;
    private String reason; 

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