package com.hiby3.pontoapi.controller;

import com.hiby3.pontoapi.model.User;
import com.hiby3.pontoapi.model.dto.PunchEventRequestDTO;
import com.hiby3.pontoapi.service.PunchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hiby3.pontoapi.model.dto.PunchEditRequestDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/punches") 
public class PunchController {

    private final PunchService punchService;

    public PunchController(PunchService punchService) {
        this.punchService = punchService;
    }

    /**
     * Endpoint para um funcionário "bater o ponto" (registrar um evento).
     * URL: POST http://localhost:8080/api/punches/event
     */
    
    @PostMapping("/event")
    public ResponseEntity<Void> registerPunchEvent(
            @RequestBody PunchEventRequestDTO request,
            Authentication authentication 
    ) { 
        User loggedInUser = (User) authentication.getPrincipal();
        punchService.registerPunchEvent(loggedInUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/edit/{punchId}")
    public ResponseEntity<Void> requestPunchEdit(
            @PathVariable Integer punchId, 
            @RequestBody PunchEditRequestDTO request,
            Authentication authentication 
    ) {
        User loggedInUser = (User) authentication.getPrincipal();
        punchService.requestPunchEdit(loggedInUser, punchId, request);
        return ResponseEntity.ok().build();
    }
    
}