package com.hiby3.pontoapi.model.dto;

public class CreateCompanyRequestDTO {

    private String nome; 
    private String cnpj; 
    private String databaseName; 

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
}