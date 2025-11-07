package com.hiby3.pontoapi.service;

import com.hiby3.pontoapi.config.TenantDataSource;
import com.hiby3.pontoapi.model.DailyWorkSummaryDTO;
import com.hiby3.pontoapi.model.Empresa;
import com.hiby3.pontoapi.repository.EmpresaRepository;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.NonNull;

@Service
public class ReportService {

    private final EmpresaRepository empresaRepository;
    private final TenantDataSource tenantDataSource;

    // O Spring vai injetar o repositório e nosso orquestrador de conexões
    public ReportService(EmpresaRepository empresaRepository, TenantDataSource tenantDataSource) {
        this.empresaRepository = empresaRepository;
        this.tenantDataSource = tenantDataSource;
    }

    /**
     * Busca o relatório de resumo diário para uma empresa específica.
     * 
     * @param empresaId O ID da empresa (vindo da tabela 'empresas' no
     *                  'empresa_master')
     * @return Uma lista com os dados do relatório
     */
    
    public List<DailyWorkSummaryDTO> getDailySummaryForTenant(@NonNull Integer empresaId) {

        // --- ETAPA 1: Achar o nome do banco do cliente ---
        Optional<Empresa> empresaOptional = empresaRepository.findById(empresaId);
        if (empresaOptional.isEmpty()) {
            throw new RuntimeException("Empresa com ID " + empresaId + " não encontrada.");
        }

        // Ex: "empresa_tecnova" ou "empresa_alpha"
        String tenantDatabaseName = empresaOptional.get().getDatabaseName();
        System.out.println(">>> BUSCANDO RELATÓRIO PARA: " + tenantDatabaseName);

        // --- ETAPA 2: Pegar a conexão dinâmica para esse banco ---
        DataSource tenantDataSource = this.tenantDataSource.getDataSource(tenantDatabaseName);

        // --- ETAPA 3: Executar o SQL "clássico" (JDBC) ---
        List<DailyWorkSummaryDTO> report = new ArrayList<>();
        String sql = "SELECT funcionario, data, horas_trabalhadas, total_pago FROM view_daily_work_summary";

        // Usamos try-with-resources para garantir que a conexão e o statement sejam
        // fechados
        try (Connection conn = tenantDataSource.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            // Itera sobre os resultados da view
            while (rs.next()) {
                DailyWorkSummaryDTO row = new DailyWorkSummaryDTO(
                        rs.getString("funcionario"),
                        rs.getDate("data"),
                        rs.getDouble("horas_trabalhadas"),
                        rs.getBigDecimal("total_pago"));
                report.add(row);
            }

        } catch (SQLException e) {
            // Em um app real, teríamos um tratamento de erro melhor
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar relatório: " + e.getMessage());
        }

        return report;
    }
}