// com.skillpath.exception.GlobalExceptionHandler
package com.skillpath.exception;
import com.skillpath.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
@RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler(ResourceNotFoundException.class)
 public ResponseEntity<ApiResponse<Void>> notFound(ResourceNotFoundException e) {
    return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
 }
 @ExceptionHandler(ForbiddenException.class)
 public ResponseEntity<ApiResponse<Void>> forbidden(ForbiddenException e) {
    return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
 }
 @ExceptionHandler(AiServiceException.class)
 public ResponseEntity<ApiResponse<Void>> aiService(AiServiceException e) {
    return ResponseEntity.status(502).body(ApiResponse.error(e.getMessage()));
 }
 @ExceptionHandler(StorageException.class)
 public ResponseEntity<ApiResponse<Void>> storage(StorageException e) {
    return ResponseEntity.status(502).body(ApiResponse.error(e.getMessage()));
 }
 @ExceptionHandler(IllegalArgumentException.class)
 public ResponseEntity<ApiResponse<Void>> badRequest(IllegalArgumentException e) {
    return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
 }
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
    return ResponseEntity.status(400).body(ApiResponse.error(msg));
 }
 @ExceptionHandler(Exception.class)
 public ResponseEntity<ApiResponse<Void>> unexpected(Exception e) {
    return ResponseEntity.status(500)
                            .body(ApiResponse.error("Server error: " + e.getMessage()));
 }
}