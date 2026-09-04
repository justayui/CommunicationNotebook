package com.communicationnotebook.backend.dto;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {

    public static ErrorResponse of(HttpStatusCode status, String message, String path) {
        String error = HttpStatus.resolve(status.value()) instanceof HttpStatus resolved
                ? resolved.getReasonPhrase()
                : status.toString();
        return new ErrorResponse(Instant.now(), status.value(), error, message, path);
    }
}
