package com.pizzaops.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Centralized, predictable API errors. Never expose internal stack traces to callers. */
@RestControllerAdvice
public class ApiExceptionHandler {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ApiError(Instant timestamp, int status, String error,
                           String message, String path, String field,
                           List<String> allowedValues) { }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex,
                                               HttpServletRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " is required or invalid")
                .distinct().sorted().reduce((a, b) -> a + "; " + b)
                .orElse("Invalid order request");
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", msg, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> badJson(HttpMessageNotReadableException ex,
                                            HttpServletRequest request) {
        // Jackson throws InvalidFormatException when a JSON value cannot be converted
        // to one of our catalog enums (e.g. size=EXTRA_GIGANTE).
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof InvalidFormatException invalid
                    && invalid.getTargetType() != null
                    && invalid.getTargetType().isEnum()) {
                String field = jsonFieldPath(invalid);
                List<String> allowed = Arrays.stream(invalid.getTargetType().getEnumConstants())
                        .map(value -> ((Enum<?>) value).name()).toList();
                String message = "Invalid value '" + invalid.getValue() + "' for field '"
                        + field + "'. Allowed values: " + String.join(", ", allowed) + ".";
                return error(HttpStatus.BAD_REQUEST, "INVALID_CATALOG_VALUE", message,
                        request, field, allowed);
            }
        }
        return error(HttpStatus.BAD_REQUEST, "INVALID_JSON",
                "Invalid JSON body or incompatible field type. Check the request syntax and field types.",
                request);
    }

    /** Examples: size, toppings[0], toppings[2]. */
    private static String jsonFieldPath(JsonMappingException exception) {
        StringBuilder field = new StringBuilder();
        for (JsonMappingException.Reference part : exception.getPath()) {
            if (part.getFieldName() != null) {
                if (field.length() > 0) field.append('.');
                field.append(part.getFieldName());
            } else if (part.getIndex() >= 0) {
                field.append('[').append(part.getIndex()).append(']');
            }
        }
        return field.isEmpty() ? "unknown" : field.toString();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> invalidOrder(IllegalArgumentException ex,
                                                 HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_ORDER", ex.getMessage(), request);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> missingOrder(OrderNotFoundException ex,
                                                 HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", ex.getMessage(), request);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code,
                                                  String message, HttpServletRequest request) {
        return error(status, code, message, request, null, null);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code,
                                                  String message, HttpServletRequest request,
                                                  String field, List<String> allowedValues) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(), status.value(), code, message,
                request.getRequestURI(), field, allowedValues));
    }
}
