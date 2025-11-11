package com.hiby3.pontoapi.service;

import com.hiby3.pontoapi.model.Employee;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import com.hiby3.pontoapi.config.TenantDataSource;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

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

    /**
     * Cadastra um novo funcionário.
     * Esta é a nossa lógica "dois-em-um".
     * 
     * @param request      O DTO com os dados do novo funcionário.
     * @param loggedInUser O usuário (RH) que está fazendo a requisição.
     */

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
        System.out.println(">>> (Mestre) Usuário " + request.getEmail() + " criado (Passo 1/2).");
        String tenantDatabaseName = empresaDoRh.getDatabaseName();
        DataSource tenantDataSource = this.tenantDataSource.getDataSource(tenantDatabaseName);
        String sql = "INSERT INTO employee (name, hourly_rate) VALUES (?, ?)";

        Integer generatedEmployeeId = null;

        try (Connection conn = tenantDataSource.getConnection();
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
            System.out.println(">>> (Cliente " + tenantDatabaseName + ") Funcionário " + request.getName()
                    + " criado com ID: " + generatedEmployeeId);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao inserir funcionário no banco do cliente: " + e.getMessage());
        }

        if (generatedEmployeeId != null) {
            novoUsuario.setClientEmployeeId(generatedEmployeeId);
            userRepository.save(novoUsuario);
            System.out.println(">>> (Mestre) Usuário " + request.getEmail() + " atualizado com client_employee_id: "
                    + generatedEmployeeId + " (Passo 2/2).");
        }
    }

    // Em EmployeeService.java

    // ... (construtor existente) ...

    /**
     * Lista todos os funcionários do banco de dados do cliente do RH logado.
     * 
     * @param loggedInRhUser O usuário (RH) que está logado.
     * @return Uma lista de objetos Employee.
     */
    public List<Employee> listEmployees(User loggedInRhUser) {

        Empresa empresaDoRh = loggedInRhUser.getEmpresa();
        if (empresaDoRh == null) {
            return Collections.emptyList(); // Retorna vazio se não houver empresa ligada
        }
        String tenantDatabaseName = empresaDoRh.getDatabaseName();

        System.out.println(">>> (Cliente " + tenantDatabaseName + ") RH buscando lista de funcionários.");

        DataSource tenantDb = tenantDataSource.getDataSource(tenantDatabaseName);
        List<Employee> employeeList = new ArrayList<>();

        // SQL: Seleciona todos da tabela employee
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
            e.printStackTrace();
            throw new RuntimeException("Erro ao listar funcionários: " + e.getMessage());
        }

        return employeeList;
    }

    // ... (método createEmployee existente) ...
}