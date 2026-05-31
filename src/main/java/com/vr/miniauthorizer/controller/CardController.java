package com.vr.miniauthorizer.controller;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vr.miniauthorizer.exception.CardException;
import com.vr.miniauthorizer.model.CardModel;
import com.vr.miniauthorizer.service.CardService;

import lombok.extern.slf4j.Slf4j;

/**
 * REST controller exposing card management endpoints.
 *
 * <p>Handles HTTP request routing, input validation, and response assembly.
 * Business logic is fully delegated to {@link CardService}.</p>
 *
 * <p>Exception Handling:
 * <ul>
 *   <li>{@link CardException.CardAlreadyExistsException} → HTTP 422 with original card body
 *       (controller-local handler, required to echo the request payload)</li>
 *   <li>{@link CardException.CardNotFoundException} → HTTP 404
 *       (controller-local handler, overrides the global 422 handler for GET endpoints)</li>
 *   <li>All other exceptions → handled by
 *       {@link com.vr.miniauthorizer.exception.GlobalExceptionHandler}</li>
 * </ul>
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /cartoes — Create a new card</li>
 *   <li>GET /cartoes/{cardNumber} — Get card balance</li>
 * </ul>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 * @see CardService for business logic
 */
@RestController
@RequestMapping("/cartoes")
@Validated
@Slf4j
public class CardController {

    private final CardService service;

    /**
     * Constructor-based injection of the card service.
     *
     * @param service the card business logic service, must be non-null
     */
    public CardController(final CardService service) {
        this.service = Objects.requireNonNull(service, "CardService cannot be null");
    }

    /**
     * Creates a new pre-paid card with the configured initial balance.
     *
     * <p>On success: returns HTTP 201 CREATED with the card model as JSON body.</p>
     * <p>On conflict: {@link #handleCardAlreadyExists} returns HTTP 422 with the request body.</p>
     *
     * @param card the card creation request with card number and plain-text password
     * @return HTTP 201 CREATED with the created {@link CardModel} as body
     */
    @PostMapping
    public ResponseEntity<CardModel> createCard(@Valid @RequestBody final CardModel card) {
        log.info("POST /cartoes — creating card. cardNumber={}", card.cardNumber());
        final CardModel created = service.createCard(card);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Returns the current available balance for the given card number.
     *
     * <p>On success: returns HTTP 200 OK with the balance as a JSON number.</p>
     * <p>On not found: {@link #handleCardNotFound} returns HTTP 404.</p>
     *
     * @param cardNumber the card number path variable, must be non-blank
     * @return HTTP 200 OK with balance as {@link BigDecimal}
     */
    @GetMapping("/{cardNumber}")
    public ResponseEntity<BigDecimal> checkBalance(@PathVariable final String cardNumber) {
        log.debug("GET /cartoes/{} — checking balance", cardNumber);
        final BigDecimal balance = service.checkBalance(cardNumber);
        return ResponseEntity.ok(balance);
    }

    /**
     * Controller-local handler for {@link CardException.CardNotFoundException}.
     *
     * <p>Returns HTTP 404 NOT FOUND. This handler takes precedence over
     * {@link com.vr.miniauthorizer.exception.GlobalExceptionHandler#handleCardNotFound}
     * for requests processed by this controller, ensuring GET /cartoes/{id} correctly
     * returns 404 when the card does not exist.</p>
     *
     * @param exception the thrown exception (not used in response body)
     * @return HTTP 404 NOT FOUND with no body
     */
    @ExceptionHandler(CardException.CardNotFoundException.class)
    public ResponseEntity<Void> handleCardNotFound(final CardException.CardNotFoundException exception) {
        log.warn("Card not found. message={}", exception.getMessage());
        return ResponseEntity.notFound().build();
    }

    /**
     * Controller-local handler for {@link CardException.CardAlreadyExistsException}.
     *
     * <p>Returns HTTP 422 UNPROCESSABLE ENTITY with the original card model as body,
     * fulfilling the API contract defined in {@code openapi.yaml} (section POST /cartoes 422).</p>
     *
     * @param exception the exception carrying the conflicting card model
     * @return HTTP 422 UNPROCESSABLE ENTITY with the original {@link CardModel} as body
     */
    @ExceptionHandler(CardException.CardAlreadyExistsException.class)
    public ResponseEntity<CardModel> handleCardAlreadyExists(
            final CardException.CardAlreadyExistsException exception) {
        log.warn("Card already exists. cardNumber={}", exception.getCardModel().cardNumber());
        return ResponseEntity.unprocessableEntity().body(exception.getCardModel());
    }
}
