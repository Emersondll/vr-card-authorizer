package com.vr.miniauthorizer.controller;

import java.util.Objects;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vr.miniauthorizer.model.TransactionModel;
import com.vr.miniauthorizer.service.TransactionService;

import lombok.extern.slf4j.Slf4j;

/**
 * REST controller exposing transaction processing endpoints.
 *
 * <p>Handles HTTP request routing, input validation, and response assembly.
 * Business logic and authorization are fully delegated to {@link TransactionService}.</p>
 *
 * <p>Exception Handling: All business rule violations ({@code CardNotFoundException},
 * {@code PasswordException}, {@code BalanceException}) are handled by
 * {@link com.vr.miniauthorizer.exception.GlobalExceptionHandler}, which returns
 * HTTP 422 with the appropriate reason string.</p>
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /transacoes — Authorize and execute a debit transaction</li>
 * </ul>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 * @see TransactionService for authorization business logic
 */
@RestController
@RequestMapping("/transacoes")
@Validated
@Slf4j
public class TransactionController {

    private final TransactionService service;

    /**
     * Constructor-based injection of the transaction service.
     *
     * @param service the transaction authorization service, must be non-null
     */
    public TransactionController(final TransactionService service) {
        this.service = Objects.requireNonNull(service, "TransactionService cannot be null");
    }

    /**
     * Authorizes and executes a debit transaction on a pre-paid card.
     *
     * <p>On success: returns HTTP 201 CREATED with body {@code "OK"}.</p>
     * <p>On authorization failure: {@link com.vr.miniauthorizer.exception.GlobalExceptionHandler}
     * returns HTTP 422 with the rejection reason ({@code "CARTAO_INEXISTENTE"},
     * {@code "SENHA_INVALIDA"}, or {@code "SALDO_INSUFICIENTE"}).</p>
     *
     * @param transactionModel the transaction request with card number, password, and amount
     * @return HTTP 201 CREATED with plain string {@code "OK"}
     */
    @PostMapping
    public ResponseEntity<String> performTransaction(
            @Valid @RequestBody final TransactionModel transactionModel) {
        log.info("POST /transacoes — processing transaction. cardNumber={}, amount={}",
                transactionModel.cardNumber(), transactionModel.amount());
        service.performTransaction(transactionModel);
        return ResponseEntity.status(HttpStatus.CREATED).body("OK");
    }
}
