package com.hiby3.pontoapi.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users") // Mapeia para a tabela "users" que criamos
public class User implements UserDetails { // <-- A MÁGICA ACONTECE AQUI

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String email; // Mapeia a coluna "email"

    @Column(nullable = false)
    private String password; // Mapeia a coluna "password"

    @Enumerated(EnumType.STRING) // Diz ao JPA para salvar a Role como "RH", "ADMIN", etc.
    @Column(nullable = false)
    private UserRole role; // Mapeia a coluna "role" usando nossa Enum

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    @Column(name = "client_employee_id")
    private Integer clientEmployeeId; // O ID do funcionário no banco do CLIENTE

    public Integer getClientEmployeeId() {
        return clientEmployeeId;
    }

    public void setClientEmployeeId(Integer clientEmployeeId) {
        this.clientEmployeeId = clientEmployeeId;
    }

    // --- Construtores, Getters e Setters ---

    public User() {
    }

    public User(String email, String password, UserRole role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    // --- MÉTODOS OBRIGATÓRIOS DO "UserDetails" (Spring Security) ---
    // O Spring vai chamar estes métodos para saber como "ler" nosso usuário

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Este método informa ao Spring qual é a "Hierarquia" (Cargo) do usuário.
        // Se for ADMIN, ele pode tudo. Se for RH, pode A, B, C. Se for FUNCIONARIO, só
        // D, E, F.
        if (this.role == UserRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_RH"),
                    new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
        } else if (this.role == UserRole.RH) {
            return List.of(new SimpleGrantedAuthority("ROLE_RH"), new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
        } else { // FUNCIONARIO
            return List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
        }
    }

    @Override
    public String getPassword() {
        return this.password; // Retorna a senha (criptografada)
    }

    @Override
    public String getUsername() {
        return this.email; // Nosso "username" é o email
    }

    // Para este SaaS, vamos deixar as contas sempre ativas
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}