package com.hiby3.pontoapi.model.dto;

public class CreateCompanyRequestDTO {

    private String nome; // Ex: "Gama Solutions"
    private String cnpj; // Ex: "12.345.678/0001-99"
    private String databaseName; // Ex: "empresa_gama"

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
}