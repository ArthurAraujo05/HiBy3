package com.hiby3.pontoapi.controller;

import com.hiby3.pontoapi.model.dto.CreateCompanyRequestDTO;
import com.hiby3.pontoapi.model.dto.RegisterRhRequestDTO; // <-- NOVO IMPORT
import com.hiby3.pontoapi.service.AdminService;
import com.hiby3.pontoapi.service.RhRegistrationService; // <-- NOVO IMPORT
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin") 
public class AdminController {

    private final AdminService adminService;
    private final RhRegistrationService rhRegistrationService; // <-- INJEÇÃO DO SERVIÇO

    // Construtor: Injeta ambos os serviços
    public AdminController(AdminService adminService, RhRegistrationService rhRegistrationService) {
        this.adminService = adminService;
        this.rhRegistrationService = rhRegistrationService;
    }

    /**
     * Endpoint 1: ADMIN CRIA UMA NOVA EMPRESA (PROVISIONAMENTO)
     * URL: POST http://localhost:8080/api/admin/companies
     * Cargo Requerido: ROLE_ADMIN
     */
    @PostMapping("/companies")
    public ResponseEntity<Void> createCompany(
            @RequestBody CreateCompanyRequestDTO request
    ) {
        adminService.createNewCompany(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    /**
     * Endpoint 2: ADMIN CRIA UM NOVO USUÁRIO RH E O ASSOCIA À EMPRESA
     * URL: POST http://localhost:8080/api/admin/rh
     * Cargo Requerido: ROLE_ADMIN
     */
    @PostMapping("/rh")
    public ResponseEntity<Void> registerRh(
            @RequestBody RegisterRhRequestDTO request
    ) {
        // Esta é a lógica final: O ADMIN cria o RH e o associa ao Tenant
        rhRegistrationService.registerNewRh(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}