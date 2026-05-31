package com.vr.miniauthorizer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

/**
 * Immutable record representing a card creation request and response body.
 *
 * <p>Used both as the inbound request payload (POST /cartoes) and as the
 * response body on success (HTTP 201) or conflict (HTTP 422), matching the
 * API contract defined in {@code openapi.yaml}.</p>
 *
 * <p>Immutability: As a Java record, all fields are {@code final} and no
 * setters are generated. Safe to share across threads.</p>
 *
 * <p>Serialization: Jackson deserializes {@code numeroCartao} → {@code cardNumber}
 * and {@code senha} → {@code password} via {@link JsonProperty}.</p>
 *
 * @param password   The card PIN in plain text (received from client).
 *                   Validated as non-blank. Hashed with SHA-256 before storage.
 *                   JSON property: {@code "senha"}.
 * @param cardNumber The 16-digit card number acting as the unique identifier.
 *                   Validated as non-blank. Used as MongoDB document ID.
 *                   JSON property: {@code "numeroCartao"}.
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 * @see com.vr.miniauthorizer.service.CardService#createCard for usage
 */
public record CardModel(
        @NotBlank(message = "Password is required")
        @JsonProperty("senha")
        String password,

        @NotBlank(message = "Card number is required")
        @JsonProperty("numeroCartao")
        String cardNumber) {

    /**
     * Compact constructor — no-op body; Jakarta Validation annotations handle
     * null/blank checks via {@code @Valid} on {@code @RequestBody}.
     */
    public CardModel {
        // Jakarta Validation annotations handle null/blank checks via @Valid on @RequestBody
    }
}
