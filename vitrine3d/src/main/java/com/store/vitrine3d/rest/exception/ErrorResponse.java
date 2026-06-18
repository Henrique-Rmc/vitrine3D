package com.store.vitrine3d.rest.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

@Getter
public class ErrorResponse {

    private final Instant timestamp = Instant.now();
    private final int status;
    private final String error;
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> fields;

    private ErrorResponse(int status, String error, String code, String message) {
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
    }

    public static ErrorResponse of(HttpStatus httpStatus, String code, String message) {
        return new ErrorResponse(httpStatus.value(), httpStatus.getReasonPhrase(), code, message);
    }

    public ErrorResponse withFields(Map<String, String> fields) {
        this.fields = fields;
        return this;
    }
}
