package com.hiby3.pontoapi.service;

import com.hiby3.pontoapi.config.TenantDataSource;
import com.hiby3.pontoapi.model.Empresa;
import com.hiby3.pontoapi.model.User;
import com.hiby3.pontoapi.model.dto.PunchEditRequestDTO;
import com.hiby3.pontoapi.model.dto.PunchEventRequestDTO;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import com.hiby3.pontoapi.model.dto.PendingPunchDTO;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;

@Service
public class PunchService {

    private final TenantDataSource tenantDataSource;

    public PunchService(TenantDataSource tenantDataSource) {
        this.tenantDataSource = tenantDataSource;
    }

    /**
     * Registra um novo evento de ponto (ENTRADA, SAIDA, etc.) para o funcionário
     * logado.
     * Este é o método principal de "bater o ponto".
     */
    public void registerPunchEvent(User loggedInUser, PunchEventRequestDTO request) {

        Integer employeeId = loggedInUser.getClientEmployeeId();
        String tenantDatabaseName = loggedInUser.getEmpresa().getDatabaseName();

        if (employeeId == null) {
            throw new RuntimeException("Este usuário não é um funcionário e não pode bater o ponto.");
        }

        String eventType = request.getEventType().toUpperCase();
        if (!eventType.equals("ENTRADA") && !eventType.equals("INICIO_INTERVALO") &&
                !eventType.equals("FIM_INTERVALO") && !eventType.equals("SAIDA")) {
            throw new IllegalArgumentException("Tipo de evento de ponto inválido: " + request.getEventType());
        }

        System.out.println(">>> (Cliente " + tenantDatabaseName + ") Registrando Evento '" + eventType +
                "' para employee_id: " + employeeId);

        DataSource tenantDb = tenantDataSource.getDataSource(tenantDatabaseName);

        String sql = "INSERT INTO punch (employee_id, timestamp, event_type) VALUES (?, ?, ?)";

        try (Connection conn = tenantDb.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setTimestamp(2, Timestamp.from(Instant.now()));
            stmt.setString(3, eventType);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao registrar batida de ponto: " + e.getMessage());
        }
    } // <-- ESTA CHAVE FECHA o método registerPunchEvent

    /**
     * Solicita a EDIÇÃO de uma batida de ponto existente.
     * Isso é o que o funcionário chama quando "esqueci de bater o ponto".
     */
    public void requestPunchEdit(User loggedInUser, Integer punchId, PunchEditRequestDTO request) {

        Integer employeeId = loggedInUser.getClientEmployeeId();
        String tenantDatabaseName = loggedInUser.getEmpresa().getDatabaseName();

        if (employeeId == null) {
            throw new RuntimeException("Este usuário não é um funcionário.");
        }

        System.out.println(">>> (Cliente " + tenantDatabaseName + ") Solicitando EDIÇÃO para punch_id: " + punchId +
                " (pertencente ao employee_id: " + employeeId + ")");

        DataSource tenantDb = tenantDataSource.getDataSource(tenantDatabaseName);

        String sql = "UPDATE punch SET " +
                "  status = 'PENDENTE_EDICAO', " +
                "  requested_timestamp = ?, " +
                "  edit_reason = ?, " +
                "  reviewed_by_rh_id = NULL " +
                "WHERE id = ? AND employee_id = ?";

        try (Connection conn = tenantDb.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(request.getRequestedTimestamp()));
            stmt.setString(2, request.getReason());
            stmt.setInt(3, punchId);
            stmt.setInt(4, employeeId); // TRAVA DE SEGURANÇA

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException("Batida de ponto não encontrada ou não pertence a este funcionário.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao solicitar edição do ponto: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException("Formato de data/hora inválido. Use 'YYYY-MM-DD HH:MM:SS'.");
        }
    }

    /**
     * APROVA uma solicitação de edição de ponto.
     * Esta API é chamada pelo RH.
     *
     * @param rhUser  O usuário (RH) que está logado e fazendo a aprovação.
     * @param punchId O ID da batida de ponto que está sendo aprovada.
     */
    public void approvePunchEdit(User rhUser, Integer punchId) {

        // 1. Pega as informações da empresa do RH
        Empresa empresaDoRh = rhUser.getEmpresa();
        if (empresaDoRh == null) {
            throw new RuntimeException("Usuário de RH não está associado a nenhuma empresa.");
        }
        String tenantDatabaseName = empresaDoRh.getDatabaseName();

        System.out.println(">>> (Cliente " + tenantDatabaseName + ") RH (User ID: " + rhUser.getId() +
                ") está APROVANDO o punch_id: " + punchId);

        // 2. Pega a conexão para o banco do cliente
        DataSource tenantDb = tenantDataSource.getDataSource(tenantDatabaseName);

        // 3. Prepara o SQL para ATUALIZAR a batida
        // (Mudamos o status para 'APROVADO',
        // COPIAMOS o 'requested_timestamp' para o 'timestamp' original,
        // e salvamos quem foi o RH que aprovou)
        String sql = "UPDATE punch p " +
                "JOIN employee e ON p.employee_id = e.id " +
                "JOIN empresa_master.users u ON e.id = u.client_employee_id " + // Junção complexa!
                "SET " +
                "  p.status = 'APROVADO', " +
                "  p.timestamp = p.requested_timestamp, " + // O ponto é oficialmente alterado!
                "  p.reviewed_by_rh_id = ? " + // O ID do RH (do banco MESTRE)
                "WHERE " +
                "  p.id = ? AND " + // O ID da batida
                "  p.status = 'PENDENTE_EDICAO' AND " + // Só podemos aprovar o que está pendente
                "  u.empresa_id = ?"; // Garante que o RH só aprove pontos da SUA empresa

        try (Connection conn = tenantDb.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, rhUser.getId()); // quem aprovou
            stmt.setInt(2, punchId); // qual batida
            stmt.setInt(3, empresaDoRh.getId()); // de qual empresa

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                // Isso acontece se o 'punchId' não existe OU não está 'PENDENTE'
                throw new RuntimeException(
                        "Batida de ponto não encontrada, não pertence a esta empresa, ou não está pendente de edição.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao aprovar edição do ponto: " + e.getMessage());
        }
    }

    /**
     * REJEITA uma solicitação de edição de ponto.
     * Esta API é chamada pelo RH.
     *
     * @param rhUser  O usuário (RH) que está logado e fazendo a rejeição.
     * @param punchId O ID da batida de ponto que está sendo rejeitada.
     */
    public void rejectPunchEdit(User rhUser, Integer punchId) {

        Empresa empresaDoRh = rhUser.getEmpresa();
        if (empresaDoRh == null) {
            throw new RuntimeException("Usuário de RH não está associado a nenhuma empresa.");
        }
        String tenantDatabaseName = empresaDoRh.getDatabaseName();

        System.out.println(">>> (Cliente " + tenantDatabaseName + ") RH (User ID: " + rhUser.getId() +
                ") está REJEITANDO o punch_id: " + punchId);

        DataSource tenantDb = tenantDataSource.getDataSource(tenantDatabaseName);

        // SQL de Rejeição:
        // Mudamos o status para 'REJEITADO' e salvamos quem revisou.
        // O 'requested_timestamp' é ignorado.
        String sql = "UPDATE punch p " +
                "JOIN employee e ON p.employee_id = e.id " +
                "JOIN empresa_master.users u ON e.id = u.client_employee_id " +
                "SET " +
                "  p.status = 'REJEITADO', " +
                "  p.reviewed_by_rh_id = ? " + // O ID do RH
                "WHERE " +
                "  p.id = ? AND " + // O ID da batida
                "  p.status = 'PENDENTE_EDICAO' AND " + // Só podemos rejeitar o que está pendente
                "  u.empresa_id = ?"; // Garante que o RH só rejeite pontos da SUA empresa

        try (Connection conn = tenantDb.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, rhUser.getId()); // quem rejeitou
            stmt.setInt(2, punchId); // qual batida
            stmt.setInt(3, empresaDoRh.getId()); // de qual empresa

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Batida de ponto não encontrada, não pertence a esta empresa, ou não está pendente de edição.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao rejeitar edição do ponto: " + e.getMessage());
        }
    }

    /**
     * Busca todas as solicitações de edição de ponto PENDENTES
     * para a empresa do RH logado.
     *
     * @param rhUser O usuário (RH) que está logado.
     * @return Uma lista de DTOs com os pontos pendentes.
     */
    public List<PendingPunchDTO> getPendingEdits(User rhUser) {

        Empresa empresaDoRh = rhUser.getEmpresa();
        if (empresaDoRh == null) {
            throw new RuntimeException("Usuário de RH não está associado a nenhuma empresa.");
        }
        String tenantDatabaseName = empresaDoRh.getDatabaseName();

        System.out.println(">>> (Cliente " + tenantDatabaseName + ") RH (User ID: " + rhUser.getId() +
                ") está buscando batidas PENDENTES.");

        DataSource tenantDb = tenantDataSource.getDataSource(tenantDatabaseName);
        List<PendingPunchDTO> pendingList = new ArrayList<>();

        // SQL para buscar pendências:
        // Precisamos do nome do funcionário (da tabela employee) e dos
        // dados da batida (da tabela punch), mas SÓ da empresa deste RH.
        String sql = "SELECT " +
                "  p.id AS punch_id, " +
                "  p.timestamp AS original_timestamp, " +
                "  p.event_type, " +
                "  p.requested_timestamp, " +
                "  p.edit_reason, " +
                "  e.id AS employee_id, " +
                "  e.name AS employee_name " +
                "FROM punch p " +
                "JOIN employee e ON p.employee_id = e.id " +
                "JOIN empresa_master.users u ON e.id = u.client_employee_id " +
                "WHERE " +
                "  p.status = 'PENDENTE_EDICAO' AND " +
                "  u.empresa_id = ?"; // Filtra SÓ para a empresa do RH

        try (Connection conn = tenantDb.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, empresaDoRh.getId()); // ID da empresa do RH

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PendingPunchDTO dto = new PendingPunchDTO(
                            rs.getInt("punch_id"),
                            rs.getTimestamp("original_timestamp"),
                            rs.getString("event_type"),
                            rs.getTimestamp("requested_timestamp"),
                            rs.getString("edit_reason"),
                            rs.getInt("employee_id"),
                            rs.getString("employee_name"));
                    pendingList.add(dto);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar batidas pendentes: " + e.getMessage());
        }

        return pendingList;
    }

}