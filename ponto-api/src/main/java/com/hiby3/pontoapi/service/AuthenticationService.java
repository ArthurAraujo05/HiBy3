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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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
     */

    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()), 
                request.getRole()
        );
        userRepository.save(user);
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

        var user = userRepository.findByEmail(request.getEmail());
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponseDTO(jwtToken);
    }
}