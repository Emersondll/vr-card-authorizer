package com.vr.miniauthorizer.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

/**
 * Centralized HTTP exception handler for the mini-authorizer REST API.
 *
 * <p>Converts domain exceptions into appropriate HTTP responses following the
 * API contract defined in {@code openapi.yaml}. Controller-level
 * {@code @ExceptionHandler} methods take precedence over handlers declared here
 * when the same exception type is handled at both levels.</p>
 *
 * <p>Exception handling hierarchy (most specific wins):
 * <ol>
 *   <li>Controller-level {@code @ExceptionHandler} (declared in {@code CardController})</li>
 *   <li>This global handler</li>
 * </ol>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles {@link CardException.CardNotFoundException} raised during transaction processing.
     *
     * <p>Returns HTTP 422 with the exception message ({@code "CARTAO_INEXISTENTE"}).
     * Note: When this exception is raised from {@code CardController.checkBalance},
     * the controller-level handler overrides this and returns HTTP 404 instead.</p>
     *
     * @param exception the exception carrying the reason string
     * @return HTTP 422 UNPROCESSABLE ENTITY with the reason as plain-text body
     */
    @ExceptionHandler(CardException.CardNotFoundException.class)
    public ResponseEntity<String> handleCardNotFound(final CardException.CardNotFoundException exception) {
        log.warn("Card not found (transaction context). message={}", exception.getMessage());
        return ResponseEntity.unprocessableEntity().body(exception.getMessage());
    }

    /**
     * Handles {@link BalanceException} raised when a card has insufficient balance.
     *
     * @param exception the exception carrying {@code "SALDO_INSUFICIENTE"}
     * @return HTTP 422 UNPROCESSABLE ENTITY with {@code "SALDO_INSUFICIENTE"} as body
     */
    @ExceptionHandler(BalanceException.class)
    public ResponseEntity<String> handleInsufficientBalance(final BalanceException exception) {
        log.warn("Insufficient balance. message={}", exception.getMessage());
        return ResponseEntity.unprocessableEntity().body(exception.getMessage());
    }

    /**
     * Handles {@link PasswordException} raised when card password verification fails.
     *
     * @param exception the exception carrying {@code "SENHA_INVALIDA"}
     * @return HTTP 422 UNPROCESSABLE ENTITY with {@code "SENHA_INVALIDA"} as body
     */
    @ExceptionHandler(PasswordException.class)
    public ResponseEntity<String> handleInvalidPassword(final PasswordException exception) {
        log.warn("Invalid password. message={}", exception.getMessage());
        return ResponseEntity.unprocessableEntity().body(exception.getMessage());
    }

    /**
     * Handles validation failures from {@code @Valid @RequestBody} constraints.
     * Returns HTTP 400 BAD REQUEST with a map of field errors.
     *
     * @param exception the validation exception carrying field error details
     * @return HTTP 400 with field name → error message map as body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            final MethodArgumentNotValidException exception) {
        log.debug("Request body validation failed. errors={}", exception.getBindingResult().getErrorCount());
        final Map<String, String> errors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value"
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handles validation failures from {@code @Validated} method parameter constraints.
     * Returns HTTP 400 BAD REQUEST with the constraint violation message.
     *
     * @param exception the constraint violation exception
     * @return HTTP 400 with violation description as body
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(
            final ConstraintViolationException exception) {
        log.debug("Constraint violation. message={}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    /**
     * Catch-all handler for unexpected exceptions not matched by more specific handlers.
     *
     * <p>Logs the full stack trace at ERROR level and returns a generic message
     * to avoid leaking internal details to clients.</p>
     *
     * @param exception the unexpected exception
     * @return HTTP 500 INTERNAL SERVER ERROR with a safe generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(final Exception exception) {
        log.error("Unexpected error occurred", exception);
        return ResponseEntity.internalServerError().body("An unexpected error occurred. Please try again later.");
    }
}
