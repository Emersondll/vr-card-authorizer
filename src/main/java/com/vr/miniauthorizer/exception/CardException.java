package com.vr.miniauthorizer.exception;

import com.vr.miniauthorizer.model.CardModel;

/**
 * Hierarchy of domain exceptions for card-related business rule violations.
 *
 * <p>All subclasses extend {@link RuntimeException} so they trigger
 * transaction rollback when annotated services use {@code @Transactional}.</p>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 */
public class CardException extends RuntimeException {

    /**
     * Constructs a {@code CardException} with the given detail message.
     *
     * @param message human-readable error description
     */
    public CardException(final String message) {
        super(message);
    }

    /**
     * Constructs a {@code CardException} with no detail message.
     */
    public CardException() {
        super();
    }

    /**
     * Thrown when a card lookup finds no matching record in the repository.
     * Maps to HTTP 404 NOT FOUND when raised from a GET endpoint,
     * or HTTP 422 UNPROCESSABLE ENTITY when raised inside a transaction flow.
     */
    public static class CardNotFoundException extends CardException {

        /**
         * Constructs the exception with the reason message returned to the client.
         *
         * @param message the reason string (e.g., {@code "CARTAO_INEXISTENTE"})
         */
        public CardNotFoundException(final String message) {
            super(message);
        }
    }

    /**
     * Thrown when a card creation request is rejected because the card number
     * already exists in the repository.
     *
     * <p>Carries the original {@link CardModel} so the HTTP response can
     * echo the request body back to the caller (as required by the API contract).</p>
     *
     * Maps to HTTP 422 UNPROCESSABLE ENTITY.
     */
    public static class CardAlreadyExistsException extends CardException {

        /**
         * The original card model from the creation request.
         * Returned as the response body to satisfy the API contract.
         */
        private final CardModel cardModel;

        /**
         * Constructs the exception carrying the conflicting card data.
         *
         * @param cardModel the card model that triggered the conflict, must be non-null
         */
        public CardAlreadyExistsException(final CardModel cardModel) {
            super();
            this.cardModel = cardModel;
        }

        /**
         * Returns the card model that caused the conflict.
         *
         * @return the original request card model, never {@code null}
         */
        public CardModel getCardModel() {
            return cardModel;
        }
    }
}
