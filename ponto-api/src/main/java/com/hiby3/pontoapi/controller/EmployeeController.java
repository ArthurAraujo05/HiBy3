package com.hiby3.pontoapi.controller;

import com.hiby3.pontoapi.model.User;
import com.hiby3.pontoapi.model.dto.CreateEmployeeRequestDTO;
import com.hiby3.pontoapi.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees") 
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Endpoint para o RH (ou ADMIN) criar um novo funcionário.
     * URL: POST http://localhost:8080/api/employees
     */
    @PostMapping
    public ResponseEntity<Void> createEmployee(
            @RequestBody CreateEmployeeRequestDTO request,
            Authentication authentication 
    ) {
        User loggedInRhUser = (User) authentication.getPrincipal();

        employeeService.createEmployee(request, loggedInRhUser);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}