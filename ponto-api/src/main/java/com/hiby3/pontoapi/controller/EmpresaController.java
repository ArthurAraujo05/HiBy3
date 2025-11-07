package com.hiby3.pontoapi.controller;

import com.hiby3.pontoapi.model.Empresa;
import com.hiby3.pontoapi.repository.EmpresaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaRepository empresaRepository;

    public EmpresaController(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @GetMapping
    public List<Empresa> listarTodasEmpresas() {
        return empresaRepository.findAll();
    }
}