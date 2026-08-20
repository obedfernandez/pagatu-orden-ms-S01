package pe.edu.upeu.orden.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String,String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos", req, errors);
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req, null);
    }
    private ResponseEntity<ApiError> build(HttpStatus s, String msg, HttpServletRequest req, Map<String,String> errors) {
        return ResponseEntity.status(s).body(new ApiError(LocalDateTime.now(), s.value(), s.getReasonPhrase(), msg, req.getRequestURI(), MDC.get("traceId"), errors));
    }
}
