package com.hiby3.pontoapi.repository;

import com.hiby3.pontoapi.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // <-- IMPORT NECESSÁRIO

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
    
    // MÉTODO ADICIONADO (Exigido pelo AuthenticationService)
    Optional<Empresa> findByDatabaseName(String databaseName);
}