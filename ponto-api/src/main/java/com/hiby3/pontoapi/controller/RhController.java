package com.hiby3.pontoapi.controller;

import com.hiby3.pontoapi.model.User;
import com.hiby3.pontoapi.service.PunchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hiby3.pontoapi.model.dto.PendingPunchDTO;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@RestController
@RequestMapping("/api/rh") 
public class RhController {

    private final PunchService punchService;
    public RhController(PunchService punchService) {
        this.punchService = punchService;
    }

    /**
     * Endpoint para o RH APROVAR uma batida de ponto pendente.
     * URL: PUT http://localhost:8080/api/rh/punches/approve/{punchId}
     */

    @PutMapping("/punches/approve/{punchId}")
    public ResponseEntity<Void> approvePunchEdit(
            @PathVariable Integer punchId, 
            Authentication authentication 
    ) {
        User loggedInRhUser = (User) authentication.getPrincipal();
        punchService.approvePunchEdit(loggedInRhUser, punchId);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint para o RH REJEITAR uma batida de ponto pendente.
     * URL: PUT http://localhost:8080/api/rh/punches/reject/{punchId}
     */

    @PutMapping("/punches/reject/{punchId}")
    public ResponseEntity<Void> rejectPunchEdit(
            @PathVariable Integer punchId,
            Authentication authentication) {
        User loggedInRhUser = (User) authentication.getPrincipal();
        punchService.rejectPunchEdit(loggedInRhUser, punchId);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint para o RH buscar a LISTA de todas as batidas pendentes
     * da sua empresa.
     * URL: GET http://localhost:8080/api/rh/punches/pending
     */

    @GetMapping("/punches/pending")
    public ResponseEntity<List<PendingPunchDTO>> getPendingPunchEdits(
            Authentication authentication 
    ) {

        User loggedInRhUser = (User) authentication.getPrincipal();
        List<PendingPunchDTO> pendingList = punchService.getPendingEdits(loggedInRhUser);
        return ResponseEntity.ok(pendingList);
    }
}