package com.hiby3.pontoapi.service;

import com.hiby3.pontoapi.model.Empresa;
import com.hiby3.pontoapi.model.User;
import com.hiby3.pontoapi.model.UserRole;
import com.hiby3.pontoapi.model.dto.RegisterRhRequestDTO;
import com.hiby3.pontoapi.repository.EmpresaRepository;
import com.hiby3.pontoapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    public void registerNewRh(RegisterRhRequestDTO request) {

        Integer empresaId = Objects.requireNonNull(request.getEmpresaId(), "O ID da empresa não pode ser nulo.");

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada com ID: " + empresaId));

        User novoRh = new User();
        novoRh.setEmail(request.getEmail());
        novoRh.setPassword(passwordEncoder.encode(request.getPassword()));
        novoRh.setRole(UserRole.RH);
        novoRh.setEmpresa(empresa);
        novoRh.setClientEmployeeId(null);
        userRepository.save(novoRh);
        System.out.println(
                ">>> (Mestre) Novo RH " + request.getEmail() + " cadastrado para a empresa " + empresa.getNome());
    }
}