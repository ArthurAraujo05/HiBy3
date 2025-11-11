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
import com.hiby3.pontoapi.model.PunchRecord;
import java.util.Collections;

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
        if (!eventType.equals("ENTRADA") && !eventType.equals("INICIO-INTERVALO") &&
                !eventType.equals("FIM-INTERVALO") && !eventType.equals("SAIDA")) {
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
    }

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
        DataSource tenantDb = tenantDataSource.getDataSource(tenantDatabaseName);

        String sql = "UPDATE punch p " +
                "JOIN employee e ON p.employee_id = e.id " +
                "JOIN empresa_master.users u ON e.id = u.client_employee_id " +
                "SET " +
                "  p.status = 'APROVADO', " +
                "  p.timestamp = p.requested_timestamp, " +
                "  p.reviewed_by_rh_id = ? " +
                "WHERE " +
                "  p.id = ? AND " +
                "  p.status = 'PENDENTE_EDICAO' AND " +
                "  u.empresa_id = ?";

        try (Connection conn = tenantDb.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, rhUser.getId());
            stmt.setInt(2, punchId);
            stmt.setInt(3, empresaDoRh.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
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
        String sql = "UPDATE punch p " +
                "JOIN employee e ON p.employee_id = e.id " +
                "JOIN empresa_master.users u ON e.id = u.client_employee_id " +
                "SET " +
                "  p.status = 'REJEITADO', " +
                "  p.reviewed_by_rh_id = ? " +
                "WHERE " +
                "  p.id = ? AND " +
                "  p.status = 'PENDENTE_EDICAO' AND " +
                "  u.empresa_id = ?";

        try (Connection conn = tenantDb.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, rhUser.getId());
            stmt.setInt(2, punchId);
            stmt.setInt(3, empresaDoRh.getId());

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
                "  u.empresa_id = ?";

        try (Connection conn = tenantDb.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, empresaDoRh.getId());

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

    /**
     * Lista todos os eventos de ponto (histórico) para um funcionário específico.
     * 
     * @param loggedInRhUser   O RH que está a consultar.
     * @param targetEmployeeId O ID do funcionário a ser consultado (do banco de
     *                         cliente).
     * @return Lista de PunchRecord.
     */
    public List<PunchRecord> getEmployeePunchHistory(User loggedInRhUser, Integer targetEmployeeId) {

        Empresa empresaDoRh = loggedInRhUser.getEmpresa();
        if (empresaDoRh == null)
            return Collections.emptyList();
        String tenantDatabaseName = empresaDoRh.getDatabaseName();

        System.out.println(
                ">>> (Cliente " + tenantDatabaseName + ") RH buscando histórico para employee_id: " + targetEmployeeId);

        DataSource tenantDb = tenantDataSource.getDataSource(tenantDatabaseName);
        List<PunchRecord> history = new ArrayList<>();

        // SQL: Seleciona todos os eventos de um funcionário específico
        String sql = "SELECT id, timestamp, event_type, status FROM punch WHERE employee_id = ? ORDER BY timestamp DESC";

        try (Connection conn = tenantDb.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, targetEmployeeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new PunchRecord(
                            rs.getInt("id"),
                            rs.getTimestamp("timestamp"),
                            rs.getString("event_type"),
                            rs.getString("status")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar histórico do ponto: " + e.getMessage());
        }

        return history;
    }

}