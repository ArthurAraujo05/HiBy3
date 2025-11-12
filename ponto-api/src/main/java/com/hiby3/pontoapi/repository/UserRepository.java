package com.hiby3.pontoapi.repository;

import com.hiby3.pontoapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import java.util.Optional; 

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    UserDetails findByEmail(String email);
    void deleteByEmail(String email);
    Optional<User> findByClientEmployeeId(Integer clientEmployeeId);
    
}