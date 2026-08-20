package pe.edu.upeu.orden.exception;
import java.time.LocalDateTime;
import java.util.Map;
public record ApiError(LocalDateTime timestamp, int status, String error, String message, String path, String traceId, Map<String,String> validationErrors) {}
