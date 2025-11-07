package com.hiby3.pontoapi.service;

import com.hiby3.pontoapi.model.User;
import com.hiby3.pontoapi.model.dto.AuthenticationResponseDTO;
import com.hiby3.pontoapi.model.dto.LoginRequestDTO;
import com.hiby3.pontoapi.model.dto.RegisterRequestDTO;
import com.hiby3.pontoapi.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    // Nossas ferramentas de segurança
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // Injetando tudo via construtor
    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Lógica para REGISTRAR um novo usuário
     * (Chamado pelo RH, por exemplo)
     */
    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        // 1. Cria um novo objeto User com os dados do DTO
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()), // CRIPTOGRAFA a senha
                request.getRole()
        );

        // 2. Salva o novo usuário no banco de dados (na tabela 'users')
        userRepository.save(user);

        // 3. Gera um token JWT para o novo usuário
        var jwtToken = jwtService.generateToken(user);

        // 4. Retorna o token
        return new AuthenticationResponseDTO(jwtToken);
    }

    /**
     * Lógica para AUTENTICAR (logar) um usuário existente
     */
    public AuthenticationResponseDTO login(LoginRequestDTO request) {
        // 1. O AuthenticationManager (do Spring) vai:
        //    a. Chamar nosso userDetailsService (que usa o UserRepository) para buscar o usuário pelo email
        //    b. Chamar nosso passwordEncoder para comparar a senha do DTO com a senha do banco
        //    c. Se falhar (email não existe ou senha errada), ele joga uma exceção.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Se chegamos aqui, o usuário foi autenticado com sucesso.
        //    Buscamos o usuário no banco (para garantir que temos o objeto completo)
        var user = userRepository.findByEmail(request.getEmail());
                //.orElseThrow(); // Em um app real, trataríamos o "usuário não encontrado"

        // 3. Geramos um token JWT para ele
        var jwtToken = jwtService.generateToken(user);

        // 4. Retornamos o token
        return new AuthenticationResponseDTO(jwtToken);
    }
}