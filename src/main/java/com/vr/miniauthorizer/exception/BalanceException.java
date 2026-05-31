package com.vr.miniauthorizer.exception;

/**
 * Thrown when a transaction is rejected due to insufficient card balance.
 *
 * <p>Maps to HTTP 422 UNPROCESSABLE ENTITY with the message
 * {@code "SALDO_INSUFICIENTE"} in the response body.</p>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 */
public class BalanceException extends RuntimeException {

    /**
     * Constructs the exception with the given detail message.
     *
     * @param message the reason string (typically {@code "SALDO_INSUFICIENTE"})
     */
    public BalanceException(final String message) {
        super(message);
    }
}
