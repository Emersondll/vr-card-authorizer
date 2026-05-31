package com.vr.miniauthorizer.service.impl;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vr.miniauthorizer.document.Card;
import com.vr.miniauthorizer.exception.CardException;
import com.vr.miniauthorizer.model.CardModel;
import com.vr.miniauthorizer.repository.CardRepository;
import com.vr.miniauthorizer.service.CardService;
import com.vr.miniauthorizer.utils.ExceptionMessages;
import com.vr.miniauthorizer.utils.HashUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link CardService} for pre-paid card management.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Card creation with password hashing and initial balance assignment</li>
 *   <li>Balance inquiry for existing cards</li>
 * </ul>
 *
 * <p>Thread Safety: This service is stateless and thread-safe.
 * All mutable state is managed by the underlying MongoDB repository.</p>
 *
 * <p>Transaction Management: Each write operation runs within its own
 * transaction via {@code @Transactional}. Rollback occurs on any
 * {@link RuntimeException}.</p>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 * @see CardRepository for persistence operations
 * @see HashUtil for password hashing
 */
@Service
@Transactional
@Slf4j
public class CardServiceImpl implements CardService {

    private final BigDecimal initialBalance;
    private final CardRepository repository;

    /**
     * Constructor-based dependency injection.
     *
     * <p>Spring automatically wires the {@code CardRepository} bean and reads
     * {@code values.standardValue} from {@code application.yml} for the initial balance.</p>
     *
     * @param repository     MongoDB repository for card documents, must be non-null
     * @param initialBalance configured initial balance applied to new cards
     *                       (property {@code values.standardValue}), must be non-null
     */
    public CardServiceImpl(
            final CardRepository repository,
            @Value("${values.standardValue}") final BigDecimal initialBalance) {
        this.repository = Objects.requireNonNull(repository, "CardRepository cannot be null");
        this.initialBalance = Objects.requireNonNull(initialBalance, "Initial balance cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementation details:
     * <ol>
     *   <li>Checks for an existing card with the same number</li>
     *   <li>Creates the MongoDB document with hashed password and initial balance</li>
     *   <li>Persists the document and returns the original request model</li>
     * </ol>
     */
    @Override
    public CardModel createCard(final CardModel cardModel) {
        Objects.requireNonNull(cardModel, "CardModel cannot be null");
        log.info("Creating card. cardNumber={}", cardModel.cardNumber());

        repository.findById(cardModel.cardNumber())
                .ifPresent(ignored -> {
                    log.warn("Card already exists. cardNumber={}", cardModel.cardNumber());
                    throw new CardException.CardAlreadyExistsException(cardModel);
                });

        final Card newCard = buildCardDocument(cardModel);
        repository.save(newCard);

        log.info("Card created successfully. cardNumber={}", cardModel.cardNumber());
        return cardModel;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Performs a read-only query and maps the result to a {@link BigDecimal}.
     * Transaction is read-only to avoid unnecessary write locks.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal checkBalance(final String cardNumber) {
        Objects.requireNonNull(cardNumber, "Card number cannot be null");
        log.debug("Checking balance. cardNumber={}", cardNumber);

        return repository.findById(cardNumber)
                .map(card -> {
                    log.debug("Balance retrieved. cardNumber={}, amount={}", cardNumber, card.getAmount());
                    return card.getAmount();
                })
                .orElseThrow(() -> {
                    log.warn("Card not found. cardNumber={}", cardNumber);
                    return new CardException.CardNotFoundException(ExceptionMessages.CARD_NOT_FOUND);
                });
    }

    /**
     * Builds a {@link Card} MongoDB document from the given {@link CardModel}.
     *
     * <p>Maps each field explicitly (no reflection-based copying) and applies
     * password hashing and initial balance assignment.</p>
     *
     * @param cardModel source card model from the HTTP request, must be non-null
     * @return a fully initialized {@link Card} document ready for persistence
     */
    private Card buildCardDocument(final CardModel cardModel) {
        final Card card = new Card();
        card.setCardNumber(cardModel.cardNumber());
        card.setPassword(HashUtil.hashString(cardModel.password()));
        card.setAmount(initialBalance);
        return card;
    }
}
