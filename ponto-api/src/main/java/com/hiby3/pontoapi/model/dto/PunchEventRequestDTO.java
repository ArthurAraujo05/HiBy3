package com.hiby3.pontoapi.model.dto;

// Este é o JSON que o funcionário vai enviar ao clicar em
// "Entrada", "Início Intervalo", etc.
public class PunchEventRequestDTO {

    // O tipo de evento: "ENTRADA", "INICIO_INTERVALO", "FIM_INTERVALO", "SAIDA"
    private String eventType;

    // Getters e Setters
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}