package com.vr.miniauthorizer.document;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a pre-paid card in the mini-authorizer system.
 *
 * <p>Each card has a unique card number (used as primary key), a hashed password
 * for transaction authentication, and a current balance in BRL.</p>
 *
 * <p>Thread Safety: Instances of this class are NOT thread-safe. Concurrent
 * modifications must be coordinated via MongoDB optimistic locking or
 * service-level transactions.</p>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 * @see com.vr.miniauthorizer.repository.CardRepository for persistence operations
 */
@Document(collection = "cards")
public class Card {

    /**
     * Unique card number used as the MongoDB document identifier.
     * Typically a 16-digit numeric string matching the card PAN.
     * Immutable after initial assignment.
     */
    @Id
    private String cardNumber;

    /**
     * SHA-256 + Base64 hashed representation of the card PIN.
     * Never stored in plain text. Compared using {@link com.vr.miniauthorizer.utils.HashUtil#compareHash}.
     */
    private String password;

    /**
     * Current available balance of the card in BRL.
     * Initialized to the configured standard value (default: 500.00).
     * Decremented on each successful transaction.
     * Never negative.
     */
    private BigDecimal amount;

    /**
     * Returns the unique card number (document primary key).
     *
     * @return card number string, never {@code null} after persistence
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * Sets the card number (document primary key).
     *
     * @param cardNumber the 16-digit card number, must be non-null
     */
    public void setCardNumber(final String cardNumber) {
        this.cardNumber = cardNumber;
    }

    /**
     * Returns the hashed card password.
     *
     * @return Base64-encoded SHA-256 hash of the card PIN
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the hashed card password.
     *
     * @param password Base64-encoded SHA-256 hash of the PIN, must be non-null
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Returns the current card balance.
     *
     * @return current balance in BRL, never {@code null} after initialization
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Sets the current card balance.
     *
     * @param amount new balance value in BRL, must be non-negative
     */
    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }
}
