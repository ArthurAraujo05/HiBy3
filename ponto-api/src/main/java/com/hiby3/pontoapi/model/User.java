package com.hiby3.pontoapi.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

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
    private Integer clientEmployeeId;

    public Integer getClientEmployeeId() {
        return clientEmployeeId;
    }

    public void setClientEmployeeId(Integer clientEmployeeId) {
        this.clientEmployeeId = clientEmployeeId;
    }

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

    // === INÍCIO: GETTERS DE LICENCIAMENTO ===

    public String getLicencaTier() {
        // Retorna o Tier da licença (TRIAL, PROFESSIONAL)
        if (this.empresa != null) {
            return this.empresa.getLicencaTier();
        }
        return null;
    }

    public java.time.LocalDate getTrialEndDate() {
        // Retorna a data de expiração do teste de 15 dias
        if (this.empresa != null) {
            return this.empresa.getTrialEndDate();
        }
        return null;
    }

    public String getStatus() {
        // Retorna o Status da Conta (ATIVO, TESTE_EXPIRADO)
        if (this.empresa != null) {
            return this.empresa.getStatus();
        }
        return null;
    }
    
    // === FIM: GETTERS DE LICENCIAMENTO ===


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
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
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

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