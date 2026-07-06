package com.store.vitrine3d.rest.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // 400 — Validation
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "Invalid value",
                        (a, b) -> a
                ));
        return badRequest(ErrorResponse.of(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "Request validation failed").withFields(fields));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fields = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> {
                            String path = v.getPropertyPath().toString();
                            int dot = path.lastIndexOf('.');
                            return dot >= 0 ? path.substring(dot + 1) : path;
                        },
                        ConstraintViolation::getMessage,
                        (a, b) -> a
                ));
        return badRequest(ErrorResponse.of(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "Request validation failed").withFields(fields));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        return badRequest(ErrorResponse.of(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "Request body is missing or contains invalid JSON"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        Map<String, String> fields = Map.of(ex.getParameterName(), "Parameter is required");
        return badRequest(ErrorResponse.of(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "Required request parameter is missing").withFields(fields));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        Map<String, String> fields = Map.of(ex.getName(),
                "Invalid value '" + ex.getValue() + "'. Expected type: " + expected);
        return badRequest(ErrorResponse.of(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "Invalid parameter type").withFields(fields));
    }

    // -------------------------------------------------------------------------
    // 401 — Authentication
    // -------------------------------------------------------------------------

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED,
                ErrorResponse.of(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password"));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return build(HttpStatus.UNAUTHORIZED,
                ErrorResponse.of(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", ex.getMessage()));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex) {
        return build(HttpStatus.UNAUTHORIZED,
                ErrorResponse.of(HttpStatus.UNAUTHORIZED, "ACCOUNT_DISABLED", "This account has been disabled"));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex) {
        return build(HttpStatus.UNAUTHORIZED,
                ErrorResponse.of(HttpStatus.UNAUTHORIZED, "ACCOUNT_LOCKED", "This account has been locked"));
    }

    // -------------------------------------------------------------------------
    // 403 — Authorization
    // -------------------------------------------------------------------------

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN,
                ErrorResponse.of(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // 404 — Not Found
    // -------------------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND,
                ErrorResponse.of(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // 405 — Method Not Allowed
    // -------------------------------------------------------------------------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED,
                ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                        "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint"));
    }

    // -------------------------------------------------------------------------
    // 409 — Conflict
    // -------------------------------------------------------------------------

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailConflict(EmailAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT,
                ErrorResponse.of(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // 415 — Unsupported Media Type
    // -------------------------------------------------------------------------

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ErrorResponse.of(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                        "Content type '" + ex.getContentType() + "' is not supported"));
    }

    // -------------------------------------------------------------------------
    // 413 — Payload Too Large
    // -------------------------------------------------------------------------

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE,
                ErrorResponse.of(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE",
                        "Image size exceeds the maximum allowed limit of 2MB"));
    }

    // -------------------------------------------------------------------------
    // 422 — Business Rule Violation
    // -------------------------------------------------------------------------

    @ExceptionHandler(InvalidImageFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidImageFormat(InvalidImageFormatException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY,
                ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_IMAGE_FORMAT", ex.getMessage()));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY,
                ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY, ex.getCode(), ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // 503 — Storage / external service failure
    // -------------------------------------------------------------------------

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        Throwable cause = ex.getCause();
        if (cause != null && (
                cause.getClass().getName().contains("minio") ||
                cause.getClass().getName().contains("Minio") ||
                cause.getClass().getName().contains("S3") ||
                (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("storage"))
        )) {
            return build(HttpStatus.SERVICE_UNAVAILABLE,
                    ErrorResponse.of(HttpStatus.SERVICE_UNAVAILABLE, "STORAGE_UNAVAILABLE",
                            "Falha ao enviar a imagem. Verifique sua conexão e tente novamente."));
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                        "An unexpected error occurred. Please try again later."));
    }

    // -------------------------------------------------------------------------
    // 500 — Internal
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                        "An unexpected error occurred. Please try again later."));
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private ResponseEntity<ErrorResponse> badRequest(ErrorResponse body) {
        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, ErrorResponse body) {
        return ResponseEntity.status(status).body(body);
    }
}
