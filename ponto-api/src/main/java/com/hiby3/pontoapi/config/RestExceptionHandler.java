package com.hiby3.pontoapi.config;

import com.hiby3.pontoapi.model.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Importante
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// @ControllerAdvice diz ao Spring que esta classe vai "ouvir" exceções
// de todos os @RestControllers
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Captura TODAS as nossas exceções de negócio (ex: "Batida não encontrada").
     * Qualquer 'throw new RuntimeException("...")' será pego aqui.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        
        // Por enquanto, qualquer RuntimeException será um 400 Bad Request
        // (Em um app real, poderíamos ter exceções customizadas)
        HttpStatus status = HttpStatus.BAD_REQUEST; // 400
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(), // <-- A MENSAGEM QUE ESCREVEMOS (ex: "Batida de ponto não encontrada...")
                request.getRequestURI()
        );
        
        // Loga o erro no console (boa prática)
        logger.error("RuntimeException: " + ex.getMessage(), ex);

        return new ResponseEntity<>(errorResponse, status);
    }
    
    /**
     * Captura exceções de segurança (ex: Token inválido ou cargo errado - 403 Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        
        HttpStatus status = HttpStatus.FORBIDDEN; // 403
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                status.value(),
                status.getReasonPhrase(),
                "Acesso Negado. Você não tem permissão para executar esta ação.", // Mensagem limpa
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Captura todas as outras exceções não tratadas (ex: Erro de banco inesperado).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                status.value(),
                status.getReasonPhrase(),
                "Ocorreu um erro inesperado no servidor.", // Mensagem segura
                request.getRequestURI()
        );
        
        logger.error("Exception não tratada: " + ex.getMessage(), ex);

        return new ResponseEntity<>(errorResponse, status);
    }
}