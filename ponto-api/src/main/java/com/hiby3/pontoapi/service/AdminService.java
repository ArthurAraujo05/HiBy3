package com.hiby3.pontoapi.service;

// Imports necessários (Flyway é novo, ResourceLoader foi removido)
import com.hiby3.pontoapi.model.Empresa;
import com.hiby3.pontoapi.model.dto.CreateCompanyRequestDTO;
import com.hiby3.pontoapi.repository.EmpresaRepository;
import org.flywaydb.core.Flyway; // <-- IMPORT NOVO
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class AdminService {

    private final EmpresaRepository empresaRepository;

    @Value("${admin.db.username}")
    private String adminUsername;
    @Value("${admin.db.password}")
    private String adminPassword;

    public AdminService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    /**
     * Provisiona uma nova empresa (tenant) no sistema.
     * Refatorado para usar o Flyway.
     */

    @Transactional
    public void createNewCompany(CreateCompanyRequestDTO request) {

        String dbName = request.getDatabaseName();
        try {
            Empresa novaEmpresa = new Empresa();
            novaEmpresa.setNome(request.getNome());
            novaEmpresa.setCnpj(request.getCnpj());
            novaEmpresa.setDatabaseName(dbName);

            empresaRepository.save(novaEmpresa);
            System.out.println(">>> (Mestre) Empresa " + request.getNome() + " salva.");

        } catch (Exception e) {
            throw new RuntimeException("Falha ao salvar no banco mestre (CNPJ duplicado?): " + e.getMessage());
        }

        try (Connection conn = getRootConnection(); Statement stmt = conn.createStatement()) {

            String sqlCreateDb = "CREATE DATABASE " + dbName;
            stmt.executeUpdate(sqlCreateDb);
            System.out.println(">>> (Servidor) Banco de dados " + dbName + " criado.");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Falha ao criar o banco de dados: " + e.getMessage());
        }

        try {
            System.out.println(">>> (Cliente " + dbName + ") Iniciando migração do Flyway...");

            String newDbUrl = "jdbc:mysql://localhost:3306/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";

            Flyway flyway = Flyway.configure()
                    .dataSource(newDbUrl, adminUsername, adminPassword)
                    .locations("classpath:db/tenant")
                    .load();

            flyway.migrate();

            System.out.println(">>> (Cliente " + dbName + ") Schema aplicado com sucesso pelo Flyway.");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Falha ao aplicar o schema com Flyway: " + e.getMessage());
        }
    }

    /**
     * Cria uma conexão "raiz" (no servidor, sem banco de dados)
     * para executar comandos DDL como CREATE DATABASE.
     */

    private Connection getRootConnection() throws SQLException {
        String rootUrl = "jdbc:mysql://localhost:3306?useSSL=false&allowPublicKeyRetrieval=true";

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(rootUrl);
        dataSource.setUsername(adminUsername);
        dataSource.setPassword(adminPassword);

        return dataSource.getConnection();
    }
}