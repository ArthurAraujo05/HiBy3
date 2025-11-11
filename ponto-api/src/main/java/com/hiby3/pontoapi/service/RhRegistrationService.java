package com.hiby3.pontoapi.service;

import com.hiby3.pontoapi.model.Empresa;
import com.hiby3.pontoapi.model.User;
import com.hiby3.pontoapi.model.UserRole;
import com.hiby3.pontoapi.model.dto.RegisterRhRequestDTO;
import com.hiby3.pontoapi.repository.EmpresaRepository;
import com.hiby3.pontoapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
public class RhRegistrationService {

    private final UserRepository userRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    public RhRegistrationService(UserRepository userRepository,
            EmpresaRepository empresaRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cria um novo usuário RH e o associa a uma empresa existente.
     * Esta API é exclusiva para ROLE_ADMIN.
     */
    
    @Transactional // Garante que tudo é salvo no banco mestre
    public void registerNewRh(RegisterRhRequestDTO request) {

        // 1. Garante que o ID não é nulo (e lança exceção se for)
        Integer empresaId = Objects.requireNonNull(request.getEmpresaId(), "O ID da empresa não pode ser nulo.");

        // 2. Procura a empresa usando o ID validado
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada com ID: " + empresaId));

        // 2. Criar o novo usuário RH
        User novoRh = new User();
        novoRh.setEmail(request.getEmail());
        novoRh.setPassword(passwordEncoder.encode(request.getPassword()));
        novoRh.setRole(UserRole.RH); // CRÍTICO: Definir o cargo correto

        // 3. Associar o RH à Empresa
        novoRh.setEmpresa(empresa);
        novoRh.setClientEmployeeId(null); // RH não é um funcionário (sem client_employee_id)

        // 4. Salvar
        userRepository.save(novoRh);
        System.out.println(
                ">>> (Mestre) Novo RH " + request.getEmail() + " cadastrado para a empresa " + empresa.getNome());
    }
}