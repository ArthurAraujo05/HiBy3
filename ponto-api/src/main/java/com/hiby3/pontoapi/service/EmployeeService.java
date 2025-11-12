package com.hiby3.pontoapi.service;

import com.hiby3.pontoapi.config.TenantDataSource;
import com.hiby3.pontoapi.model.Employee;
import com.hiby3.pontoapi.model.Empresa;
import com.hiby3.pontoapi.model.User;
import com.hiby3.pontoapi.model.UserRole;
import com.hiby3.pontoapi.model.dto.CreateEmployeeRequestDTO;
import com.hiby3.pontoapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantDataSource tenantDataSource;

    public EmployeeService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TenantDataSource tenantDataSource) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tenantDataSource = tenantDataSource;
    }

    @Transactional
    public void createEmployee(CreateEmployeeRequestDTO request, User loggedInUser) {
        Empresa empresaDoRh = loggedInUser.getEmpresa();
        if (empresaDoRh == null) {
            throw new RuntimeException("Usuário de RH não está associado a nenhuma empresa.");
        }
        User novoUsuario = new User();
        novoUsuario.setEmail(request.getEmail());
        novoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        novoUsuario.setRole(UserRole.FUNCIONARIO);
        novoUsuario.setEmpresa(empresaDoRh);
        userRepository.save(novoUsuario);

        String tenantDatabaseName = empresaDoRh.getDatabaseName();
        DataSource tenantDb = this.tenantDataSource.getDataSource(tenantDatabaseName);
        String sql = "INSERT INTO employee (name, hourly_rate) VALUES (?, ?)";
        Integer generatedEmployeeId = null;

        try (Connection conn = tenantDb.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, request.getName());
            stmt.setBigDecimal(2, request.getHourlyRate());
            stmt.executeUpdate();
            try (var generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedEmployeeId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Falha ao criar funcionário, nenhum ID obtido.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir funcionário no banco do cliente: " + e.getMessage());
        }
        if (generatedEmployeeId != null) {
            novoUsuario.setClientEmployeeId(generatedEmployeeId);
            userRepository.save(novoUsuario);
        }
    }

    public List<Employee> listEmployees(User loggedInRhUser) {
        Empresa empresaDoRh = loggedInRhUser.getEmpresa();
        if (empresaDoRh == null)
            return Collections.emptyList();
        String tenantDatabaseName = empresaDoRh.getDatabaseName();

        DataSource tenantDb = this.tenantDataSource.getDataSource(tenantDatabaseName);
        List<Employee> employeeList = new ArrayList<>();
        String sql = "SELECT id, name, hourly_rate FROM employee";

        try (Connection conn = tenantDb.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Employee employee = new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("hourly_rate"));
                employeeList.add(employee);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar funcionários: " + e.getMessage());
        }
        return employeeList;
    }

    @Transactional
    public void deleteEmployeeAndUser(User loggedInRhUser, Integer targetEmployeeId) {
        Empresa empresaDoRh = loggedInRhUser.getEmpresa();
        if (empresaDoRh == null) {
            throw new RuntimeException("Usuário RH não associado a uma empresa.");
        }
        Optional<User> userOptional = userRepository.findByClientEmployeeId(targetEmployeeId);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Funcionário não encontrado ou ID inválido.");
        }
        User userToDelete = userOptional.get();
        if (!userToDelete.getEmpresa().getId().equals(empresaDoRh.getId())) {
            throw new RuntimeException("Funcionário não pertence à sua empresa para exclusão.");
        }
        deleteEmployeeFromTenant(empresaDoRh.getDatabaseName(), targetEmployeeId);
        userRepository.delete(userToDelete);
    }

    private void deleteEmployeeFromTenant(String tenantDatabaseName, Integer employeeId) {
        DataSource tenantDb = this.tenantDataSource.getDataSource(tenantDatabaseName);
        String deletePunchesSql = "DELETE FROM punch WHERE employee_id = ?";
        String deleteEmployeeSql = "DELETE FROM employee WHERE id = ?";

        try (Connection conn = tenantDb.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtPunches = conn.prepareStatement(deletePunchesSql)) {
                stmtPunches.setInt(1, employeeId);
                stmtPunches.executeUpdate();
            }
            try (PreparedStatement stmtEmployee = conn.prepareStatement(deleteEmployeeSql)) {
                stmtEmployee.setInt(1, employeeId);
                int rowsAffected = stmtEmployee.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    throw new RuntimeException(
                            "Funcionário com ID " + employeeId + " não encontrado para exclusão (rollback no tenant).");
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Falha na exclusão do funcionário e pontos no tenant: " + e.getMessage());
        }
    }
}