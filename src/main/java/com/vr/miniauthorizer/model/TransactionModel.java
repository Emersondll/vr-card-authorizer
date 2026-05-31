package com.vr.miniauthorizer.model;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Immutable record representing a debit transaction request payload.
 *
 * <p>Received as the inbound request body for POST /transacoes. The service
 * validates card existence, password match, and sufficient balance before
 * approving the transaction.</p>
 *
 * <p>Immutability: As a Java record, all fields are {@code final}.
 * Safe to share across threads.</p>
 *
 * <p>Serialization: Jackson maps JSON fields to record components via
 * {@link JsonProperty} annotations.</p>
 *
 * @param cardNumber   The 16-digit card number to debit.
 *                     Validated as non-blank. JSON property: {@code "numeroCartao"}.
 * @param cardPassword The card PIN in plain text for authentication.
 *                     Validated as non-blank. JSON property: {@code "senhaCartao"}.
 * @param amount       The transaction amount in BRL. Must be positive.
 *                     JSON property: {@code "valor"}.
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 * @see com.vr.miniauthorizer.service.TransactionService#performTransaction for usage
 */
public record TransactionModel(
        @NotBlank(message = "Card number is required")
        @JsonProperty("numeroCartao")
        String cardNumber,

        @NotBlank(message = "Card password is required")
        @JsonProperty("senhaCartao")
        String cardPassword,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @JsonProperty("valor")
        BigDecimal amount) {

    /**
     * Compact constructor — validates all fields before record instantiation.
     *
     * @throws NullPointerException     if any field is null
     * @throws IllegalArgumentException if {@code amount} is zero or negative
     */
    public TransactionModel {
        Objects.requireNonNull(cardNumber, "Card number cannot be null");
        Objects.requireNonNull(cardPassword, "Card password cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
}
