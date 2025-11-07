package com.hiby3.pontoapi.model.dto;

import java.time.Instant;

// Este é o JSON limpo que vamos retornar para QUALQUER erro na API
public class ErrorResponseDTO {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    // Construtor
    public ErrorResponseDTO(int status, String error, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // Getters (O Jackson usa para serializar o JSON)
    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
}