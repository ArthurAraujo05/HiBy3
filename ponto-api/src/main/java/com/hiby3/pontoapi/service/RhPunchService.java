// ARQUIVO: src/main/java/com/hiby3/pontoapi/service/RhPunchService.java (CORRIGIDO)
package com.hiby3.pontoapi.service;

import com.hiby3.pontoapi.config.TenantDataSource; // <-- CORRIGIDO: NOME DA CLASSE É 'TenantDataSource'
import com.hiby3.pontoapi.model.PendingPunch;
import com.hiby3.pontoapi.model.User;
import com.hiby3.pontoapi.model.Empresa;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RhPunchService {

    private final TenantDataSource tenantDataSource; // <-- CORRIGIDO AQUI

    // O @Autowired não é necessário no construtor em versões modernas
    public RhPunchService(TenantDataSource tenantDataSource) { // <-- CORRIGIDO AQUI
        this.tenantDataSource = tenantDataSource;
    }

    /**
     * Busca todos os pontos com status 'PENDING' na base de dados do cliente do RH logado.
     * @param loggedInRhUser O usuário (RH) que está logado.
     * @return Uma lista de objetos PendingPunch.
     */
    public List<PendingPunch> listPendingPunches(User loggedInRhUser) {
        
        Empresa empresaDoRh = loggedInRhUser.getEmpresa();
        if (empresaDoRh == null) {
            return Collections.emptyList();
        }
        String tenantDatabaseName = empresaDoRh.getDatabaseName();

        System.out.println(">>> (Cliente " + tenantDatabaseName + ") RH buscando lista de PONTOS PENDENTES.");

        DataSource tenantDb = tenantDataSource.getDataSource(tenantDatabaseName);
        List<PendingPunch> pendingPunches = new ArrayList<>();
        
        // SQL: Faz um JOIN entre punch e employee para pegar o nome do funcionário
        String sql = "SELECT p.id, e.name, p.date, p.punch_in, p.punch_out, p.status " +
                     "FROM punch p JOIN employee e ON p.employee_id = e.id " +
                     "WHERE p.status = 'PENDING'";

        try (Connection conn = tenantDb.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PendingPunch punch = new PendingPunch(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getObject("date", LocalDate.class),
                    rs.getObject("punch_in", LocalTime.class),
                    rs.getObject("punch_out", LocalTime.class), 
                    rs.getString("status")
                );
                pendingPunches.add(punch);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao listar pontos pendentes: " + e.getMessage());
        }
        
        return pendingPunches;
    }
}