package com.vr.miniauthorizer.service.impl;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vr.miniauthorizer.document.Card;
import com.vr.miniauthorizer.exception.BalanceException;
import com.vr.miniauthorizer.exception.CardException;
import com.vr.miniauthorizer.exception.PasswordException;
import com.vr.miniauthorizer.model.TransactionModel;
import com.vr.miniauthorizer.repository.CardRepository;
import com.vr.miniauthorizer.service.TransactionService;
import com.vr.miniauthorizer.utils.ExceptionMessages;
import com.vr.miniauthorizer.utils.HashUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link TransactionService} for debit transaction authorization.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Card existence validation</li>
 *   <li>Password authentication via SHA-256 hash comparison</li>
 *   <li>Balance sufficiency check</li>
 *   <li>Atomic balance deduction and persistence</li>
 * </ul>
 *
 * <p>Thread Safety: This service is stateless. Concurrent transactions on the
 * same card are serialized at the MongoDB document level via the {@code @Transactional}
 * boundary.</p>
 *
 * <p>Transaction Management: {@code performTransaction} runs within a single
 * transaction. Any exception causes full rollback, preventing partial balance deductions.</p>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 * @see CardRepository for persistence operations
 * @see HashUtil for password verification
 */
@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final CardRepository repository;

    /**
     * Constructor-based dependency injection.
     *
     * @param repository MongoDB repository for card persistence, must be non-null
     */
    public TransactionServiceImpl(final CardRepository repository) {
        this.repository = Objects.requireNonNull(repository, "CardRepository cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Authorization steps:
     * <ol>
     *   <li>{@link #findCardOrThrow} — verifies card existence</li>
     *   <li>{@link #validatePassword} — verifies PIN against stored hash</li>
     *   <li>{@link #validateBalance} — verifies sufficient balance</li>
     *   <li>Deducts the amount and persists the updated card</li>
     * </ol>
     */
    @Override
    @Transactional
    public void performTransaction(final TransactionModel transactionModel) {
        Objects.requireNonNull(transactionModel, "TransactionModel cannot be null");
        log.info("Processing transaction. cardNumber={}, amount={}",
                transactionModel.cardNumber(), transactionModel.amount());

        final Card card = findCardOrThrow(transactionModel.cardNumber());
        validatePassword(transactionModel, card);
        validateBalance(transactionModel, card);

        card.setAmount(card.getAmount().subtract(transactionModel.amount()));
        repository.save(card);

        log.info("Transaction approved. cardNumber={}, amount={}, newBalance={}",
                transactionModel.cardNumber(), transactionModel.amount(), card.getAmount());
    }

    /**
     * Retrieves the card for the given number or throws {@link CardException.CardNotFoundException}.
     *
     * @param cardNumber the card number to look up, must be non-null
     * @return the persisted {@link Card} document
     * @throws CardException.CardNotFoundException if no card exists for the given number
     */
    private Card findCardOrThrow(final String cardNumber) {
        return repository.findById(cardNumber)
                .orElseThrow(() -> {
                    log.warn("Transaction rejected — card not found. cardNumber={}", cardNumber);
                    return new CardException.CardNotFoundException(ExceptionMessages.CARD_NOT_FOUND);
                });
    }

    /**
     * Validates that the transaction password matches the card's stored hash.
     *
     * @param transactionModel transaction payload containing the plain-text password
     * @param card             the card document containing the stored password hash
     * @throws PasswordException if the passwords do not match
     */
    private void validatePassword(final TransactionModel transactionModel, final Card card) {
        if (!HashUtil.compareHash(transactionModel.cardPassword(), card.getPassword())) {
            log.warn("Transaction rejected — invalid password. cardNumber={}",
                    transactionModel.cardNumber());
            throw new PasswordException(ExceptionMessages.INVALID_PASSWORD);
        }
    }

    /**
     * Validates that the card has sufficient balance for the transaction amount.
     *
     * @param transactionModel transaction payload containing the requested amount
     * @param card             the card document with the current balance
     * @throws BalanceException if the card balance is less than the transaction amount
     */
    private void validateBalance(final TransactionModel transactionModel, final Card card) {
        if (card.getAmount().compareTo(transactionModel.amount()) < 0) {
            log.warn("Transaction rejected — insufficient balance. cardNumber={}, balance={}, requested={}",
                    transactionModel.cardNumber(), card.getAmount(), transactionModel.amount());
            throw new BalanceException(ExceptionMessages.INSUFFICIENT_BALANCE);
        }
    }
}
