package com.hiby3.pontoapi.model;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "cnpj", nullable = false, unique = true)
    private String cnpj;

    @Column(name = "database_name", nullable = false)
    private String databaseName;

    public Empresa() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Column(name = "licenca_tier", nullable = false)
    private String licencaTier;

    // 2. A data em que o trial expira (necessário para o teste de 15 dias)
    @Column(name = "trial_end_date")
    private LocalDate trialEndDate;

    // 3. O status da conta (para saber se o acesso deve ser bloqueado)
    @Column(name = "status", nullable = false)
    private String status = "ATIVO"; // Ex: ATIVO, INATIVO, TESTE_EXPIRADO

    public String getLicencaTier() {
        return licencaTier;
    }

    public void setLicencaTier(String licencaTier) {
        this.licencaTier = licencaTier;
    }

    public LocalDate getTrialEndDate() {
        return trialEndDate;
    }

    public void setTrialEndDate(LocalDate trialEndDate) {
        this.trialEndDate = trialEndDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}