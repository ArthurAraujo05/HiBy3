package com.hiby3.pontoapi.service;

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
        // --- ETAPA A: Criar o usuário de login no banco MESTRE ---

        // 1. Pega a Empresa do usuário de RH que está logado
        Empresa empresaDoRh = loggedInUser.getEmpresa();
        if (empresaDoRh == null) {
            throw new RuntimeException("Usuário de RH não está associado a nenhuma empresa.");
        }

        // 2. Cria o novo usuário (FUNCIONARIO)
        User novoUsuario = new User();
        novoUsuario.setEmail(request.getEmail());
        novoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        novoUsuario.setRole(UserRole.FUNCIONARIO);
        novoUsuario.setEmpresa(empresaDoRh);

        // 3. Salva o novo usuário no 'empresa_master.users'
        userRepository.save(novoUsuario);
        System.out.println(">>> (Mestre) Usuário " + request.getEmail() + " criado (Passo 1/2).");

        // --- ETAPA B: Criar o funcionário no banco do CLIENTE ---

        // 1. Pega o nome do banco do cliente (ex: "empresa_tecnova")
        String tenantDatabaseName = empresaDoRh.getDatabaseName();
        DataSource tenantDataSource = this.tenantDataSource.getDataSource(tenantDatabaseName);
        String sql = "INSERT INTO employee (name, hourly_rate) VALUES (?, ?)";

        Integer generatedEmployeeId = null; // Variável para guardar o ID

        try (Connection conn = tenantDataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, request.getName());
            stmt.setBigDecimal(2, request.getHourlyRate());

            stmt.executeUpdate();

            // Pega o ID que o AUTO_INCREMENT acabou de gerar
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
            userRepository.save(novoUsuario); // Salva o usuário DE NOVO, agora com o ID
            System.out.println(">>> (Mestre) Usuário " + request.getEmail() + " atualizado com client_employee_id: " + generatedEmployeeId + " (Passo 2/2).");
        }
    }
}