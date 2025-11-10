package com.hiby3.pontoapi.controller;

import com.hiby3.pontoapi.model.dto.CreateCompanyRequestDTO;
import com.hiby3.pontoapi.service.AdminService;
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

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Endpoint para o ADMIN criar uma nova Empresa (Tenant).
     * URL: POST http://localhost:8080/api/admin/companies
     */
    @PostMapping("/companies")
    public ResponseEntity<Void> createCompany(
            @RequestBody CreateCompanyRequestDTO request
    ) {
        adminService.createNewCompany(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}