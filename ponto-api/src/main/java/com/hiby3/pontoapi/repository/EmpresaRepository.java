package com.hiby3.pontoapi.repository;

import com.hiby3.pontoapi.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
}