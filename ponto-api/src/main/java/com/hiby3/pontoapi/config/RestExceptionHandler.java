package com.hiby3.pontoapi.config;

import com.hiby3.pontoapi.model.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; 
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        
        HttpStatus status = HttpStatus.BAD_REQUEST; // 400
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(), 
                request.getRequestURI()
        );
        
        // Loga o erro no console 
        logger.error("RuntimeException: " + ex.getMessage(), ex);

        return new ResponseEntity<>(errorResponse, status);
    }
    
    
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


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                status.value(),
                status.getReasonPhrase(),
                "Ocorreu um erro inesperado no servidor.", 
                request.getRequestURI()
        );
        
        logger.error("Exception não tratada: " + ex.getMessage(), ex);

        return new ResponseEntity<>(errorResponse, status);
    }
}