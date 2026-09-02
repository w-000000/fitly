package com.example.minip.config;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .findFirst().map(error -> error.getField() + " 값이 올바르지 않습니다.").orElse("입력값을 확인해주세요.");
        return Map.of("timestamp", Instant.now().toString(), "status", 400, "message", message);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<Map<String, Object>> responseStatus(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        String message = exception.getReason() == null ? "요청을 처리하지 못했습니다." : exception.getReason();
        return org.springframework.http.ResponseEntity.status(exception.getStatusCode()).body(
            Map.of("timestamp", Instant.now().toString(), "status", status, "message", message)
        );
    }
}
