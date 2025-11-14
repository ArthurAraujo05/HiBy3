package com.hiby3.pontoapi.service;

import com.hiby3.pontoapi.model.Empresa; // <-- IMPORT
import com.hiby3.pontoapi.model.User;
import com.hiby3.pontoapi.model.UserRole; // <-- IMPORT
import com.hiby3.pontoapi.model.dto.AuthenticationResponseDTO;
import com.hiby3.pontoapi.model.dto.CreateCompanyRequestDTO; // <-- IMPORT
import com.hiby3.pontoapi.model.dto.LoginRequestDTO;
import com.hiby3.pontoapi.model.dto.RegisterRequestDTO;
import com.hiby3.pontoapi.repository.EmpresaRepository; // <-- IMPORT
import com.hiby3.pontoapi.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- IMPORT
import java.time.LocalDate; // <-- IMPORT
import java.util.Objects; // <-- IMPORT

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final EmpresaRepository empresaRepository; // <-- INJETADO
    private final AdminService adminService; // <-- INJETADO
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository,
                                 EmpresaRepository empresaRepository, // <-- INJETADO
                                 AdminService adminService, // <-- INJETADO
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.empresaRepository = empresaRepository;
        this.adminService = adminService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Lógica para REGISTRAR um novo usuário E provisionar a empresa (Tenant).
     * Este é o fluxo do "Teste Grátis 15 Dias".
     */
    @Transactional // Garante que ou tudo falha, ou tudo funciona (cria Empresa E Usuário)
    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        
        // 1. Validar o Plano (Deve ser TRIAL ou PROFESSIONAL)
        String planTier = Objects.requireNonNull(request.getPlanTier(), "O plano (planTier) é obrigatório.");
        if (!planTier.equals("TRIAL") && !planTier.equals("PROFESSIONAL") && !planTier.equals("BUSINESS")) {
            throw new RuntimeException("Plano de licença inválido.");
        }

        // 2. Criar a Empresa (Tenant) primeiro
        String databaseName = "empresa_" + request.getCompanyName().toLowerCase().replaceAll("[^a-z0-9]", "");
        
        CreateCompanyRequestDTO companyRequest = new CreateCompanyRequestDTO();
        companyRequest.setNome(request.getCompanyName());
        companyRequest.setCnpj("00.000.000/0000-00"); // CNPJ Padrão para Trial
        companyRequest.setDatabaseName(databaseName);

        // 3. Salva a Empresa e Cria o Banco de Dados (usando AdminService)
        adminService.createNewCompany(companyRequest);
        
        // 4. Busca a empresa recém-criada para definir a Licença
        Empresa novaEmpresa = empresaRepository.findByDatabaseName(databaseName)
                .orElseThrow(() -> new RuntimeException("Erro ao localizar empresa recém-criada."));

        // 5. Definir a Licença com base no plano
        if (planTier.equals("TRIAL")) {
            novaEmpresa.setLicencaTier("TRIAL");
            novaEmpresa.setTrialEndDate(LocalDate.now().plusDays(15)); // 15 dias de teste
            novaEmpresa.setStatus("ATIVO");
        } else {
            // Se for PROFESSIONAL ou BUSINESS, o status é PENDENTE_PAGAMENTO
            novaEmpresa.setLicencaTier(planTier);
            novaEmpresa.setStatus("PENDENTE_PAGAMENTO");
        }
        empresaRepository.save(novaEmpresa);

        // 6. Criar o Usuário (RH) e associar à Empresa
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                UserRole.RH // O primeiro usuário registrado é sempre o RH
        );
        user.setEmpresa(novaEmpresa); // Associa o usuário ao Tenant
        userRepository.save(user);

        // 7. Gerar o Token JWT (que agora terá os claims de licença)
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponseDTO(jwtToken);
    }

    /**
     * Lógica para AUTENTICAR (logar) um usuário existente
     */
    public AuthenticationResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Busca o User (que contém o objeto Empresa com a licença)
        User user = (User) userRepository.findByEmail(request.getEmail());
        
        // Gera o token (o JwtService irá incluir os claims de licença)
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponseDTO(jwtToken);
    }
}