package com.communicationnotebook.backend.exception;

import com.communicationnotebook.backend.dto.ErrorResponse;
import com.communicationnotebook.backend.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * 想定内・想定外を問わず例外発生時に必ずログへ記録し、原因追跡できるようにする。
 * Spring Boot標準のProblemDetail形式に変わらないよう、ResponseEntityExceptionHandlerは継承しない。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {
        String context = requestContext(request);
        if (ex.getStatusCode().is5xxServerError()) {
            log.error("ResponseStatusException: status={} {} reason={}", ex.getStatusCode().value(), context, ex.getReason(), ex);
        } else {
            log.warn("ResponseStatusException: status={} {} reason={}", ex.getStatusCode().value(), context, ex.getReason());
        }
        return ResponseEntity.status(ex.getStatusCode())
                .body(ErrorResponse.of(ex.getStatusCode(), ex.getReason(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String context = requestContext(request);
        log.warn("Validation failed: {} errors={}", context, ex.getBindingResult().getFieldErrorCount());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "入力内容に誤りがあります", request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        String context = requestContext(request);
        log.error("Unhandled exception: {}", context, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "サーバーエラーが発生しました", request.getRequestURI()));
    }

    private String requestContext(HttpServletRequest request) {
        return "method=%s path=%s user=%s".formatted(request.getMethod(), request.getRequestURI(), currentUserDescription());
    }

    private String currentUserDescription() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return "%s(id=%d)".formatted(principal.getUsername(), principal.getId());
        }
        return "anonymous";
    }
}
