package com.vr.miniauthorizer.exception;

/**
 * Thrown when a transaction is rejected because the provided card password
 * does not match the stored hash.
 *
 * <p>Maps to HTTP 422 UNPROCESSABLE ENTITY with the message
 * {@code "SENHA_INVALIDA"} in the response body.</p>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 */
public class PasswordException extends RuntimeException {

    /**
     * Constructs the exception with the given detail message.
     *
     * @param message the reason string (typically {@code "SENHA_INVALIDA"})
     */
    public PasswordException(final String message) {
        super(message);
    }
}
