package com.vr.miniauthorizer.utils;

/**
 * Centralized constants for exception and API error messages returned to clients.
 *
 * <p>All values are in Portuguese to match the mini-authorizer API specification.
 * They are returned as plain-text response bodies in 422 UNPROCESSABLE ENTITY responses.</p>
 *
 * <p>This is a utility class and cannot be instantiated.</p>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 */
public final class ExceptionMessages {

    /** Returned when the provided card password does not match the stored hash. */
    public static final String INVALID_PASSWORD = "SENHA_INVALIDA";

    /** Returned when the card balance is insufficient to cover the requested transaction amount. */
    public static final String INSUFFICIENT_BALANCE = "SALDO_INSUFICIENTE";

    /** Returned when no card with the requested card number is found in the repository. */
    public static final String CARD_NOT_FOUND = "CARTAO_INEXISTENTE";

    /** Internal message used when the SHA-256 algorithm is unexpectedly unavailable. */
    public static final String ERROR_WHILE_HASHING_STRING = "Error while hashing string";

    private ExceptionMessages() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
