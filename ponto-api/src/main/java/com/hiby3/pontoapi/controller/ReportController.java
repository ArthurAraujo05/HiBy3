package com.hiby3.pontoapi.controller;

import com.hiby3.pontoapi.model.DailyWorkSummaryDTO;
import com.hiby3.pontoapi.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.lang.NonNull;

@RestController
@RequestMapping("/api/reports") // A URL base para todos os relatórios
public class ReportController {

    private final ReportService reportService;

    // Injeta (o service) que criamos
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Endpoint para buscar o relatório de resumo diário de uma empresa.
     * Exemplo de URL: /api/reports/1/daily-summary
     * Exemplo de URL: /api/reports/2/daily-summary
     */
    @GetMapping("/{empresaId}/daily-summary")
    public List<DailyWorkSummaryDTO> getDailySummary(@PathVariable @NonNull Integer empresaId) {
        // @PathVariable pega o "1" ou "2" da URL e passa para o método
        
        // Chama o nosso service multi-tenant!
        return reportService.getDailySummaryForTenant(empresaId);
    }
}