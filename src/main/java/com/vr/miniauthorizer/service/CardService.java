package com.vr.miniauthorizer.service;

import java.math.BigDecimal;

import com.vr.miniauthorizer.exception.CardException;
import com.vr.miniauthorizer.model.CardModel;

/**
 * Service contract for card management operations.
 *
 * <p>Defines the business operations available for pre-paid card lifecycle management.
 * Implementations must enforce all business rules described in each method contract.</p>
 *
 * <p>All implementations of this interface are expected to be Spring-managed
 * beans annotated with {@code @Service}.</p>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 * @see com.vr.miniauthorizer.service.impl.CardServiceImpl for the default implementation
 */
public interface CardService {

    /**
     * Creates a new pre-paid card with the configured initial balance.
     *
     * <p>The card password is hashed (SHA-256) before storage.
     * The initial balance is read from application configuration
     * ({@code values.standardValue}, default: {@code 500.00}).</p>
     *
     * @param cardModel the card creation request containing card number and plain-text password;
     *                  must be non-null
     * @return the original {@link CardModel} (echoed back as per API contract)
     * @throws CardException.CardAlreadyExistsException if a card with the same number already exists
     */
    CardModel createCard(CardModel cardModel);

    /**
     * Returns the current available balance of a card.
     *
     * @param cardNumber the unique card number to query; must be non-null and non-blank
     * @return current balance in BRL as a {@link BigDecimal}, never {@code null}
     * @throws CardException.CardNotFoundException if no card with the given number exists
     */
    BigDecimal checkBalance(String cardNumber);
}
