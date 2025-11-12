package com.hiby3.pontoapi.controller;

import com.hiby3.pontoapi.model.PunchRecord;
import com.hiby3.pontoapi.model.User;
import com.hiby3.pontoapi.service.PunchService;
import com.hiby3.pontoapi.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hiby3.pontoapi.model.dto.PendingPunchDTO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/rh")
public class RhController {

    private final PunchService punchService;
    private final EmployeeService employeeService;

    public RhController(PunchService punchService, EmployeeService employeeService) {
        this.punchService = punchService;
        this.employeeService = employeeService;
    }

    @GetMapping("/punches/pending")
    public ResponseEntity<List<PendingPunchDTO>> getPendingPunchEdits(
            Authentication authentication) {
        User loggedInRhUser = (User) authentication.getPrincipal();
        List<PendingPunchDTO> pendingList = punchService.getPendingEdits(loggedInRhUser);
        return ResponseEntity.ok(pendingList);
    }

    @PutMapping("/punches/approve/{punchId}")
    public ResponseEntity<Void> approvePunchEdit(
            @PathVariable Integer punchId,
            Authentication authentication) {
        User loggedInRhUser = (User) authentication.getPrincipal();
        punchService.approvePunchEdit(loggedInRhUser, punchId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/punches/reject/{punchId}")
    public ResponseEntity<Void> rejectPunchEdit(
            @PathVariable Integer punchId,
            Authentication authentication) {
        User loggedInRhUser = (User) authentication.getPrincipal();
        punchService.rejectPunchEdit(loggedInRhUser, punchId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/employees/{employeeId}/punches")
    public ResponseEntity<List<PunchRecord>> getEmployeePunchHistory(
            @PathVariable Integer employeeId,
            Authentication authentication) {
        User loggedInRhUser = (User) authentication.getPrincipal();
        List<PunchRecord> history = punchService.getEmployeePunchHistory(loggedInRhUser, employeeId);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/employees/{employeeId}")
    public ResponseEntity<?> deleteEmployee(
            @PathVariable Integer employeeId,
            Authentication authentication) {
        User loggedInRhUser = (User) authentication.getPrincipal();
        try {
            employeeService.deleteEmployeeAndUser(loggedInRhUser, employeeId);
            return ResponseEntity.ok()
                    .body("Funcionário com ID " + employeeId + " e login associado foram excluídos com sucesso.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}