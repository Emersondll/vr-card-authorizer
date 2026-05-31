package com.vr.miniauthorizer.service.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vr.miniauthorizer.document.Card;
import com.vr.miniauthorizer.exception.BalanceException;
import com.vr.miniauthorizer.exception.CardException;
import com.vr.miniauthorizer.exception.PasswordException;
import com.vr.miniauthorizer.model.TransactionModel;
import com.vr.miniauthorizer.repository.CardRepository;

import resources.fixtures.TestFixture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionServiceImpl")
class TransactionServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(cardRepository);
    }

    @Test
    @DisplayName("should perform transaction successfully when card is valid and balance is sufficient")
    void shouldPerformTransactionSuccessfullyWhenCardIsValidAndBalanceIsSufficient() {
        Card card = TestFixture.createCard();
        card.setPassword(TestFixture.CARD_PASSWORD_HASH);
        when(cardRepository.findById(TestFixture.CARD_NUMBER)).thenReturn(Optional.of(card));

        TransactionModel transactionModel = new TransactionModel(
                TestFixture.CARD_NUMBER,
                TestFixture.CARD_PASSWORD,
                new BigDecimal(TestFixture.CARD_AMOUNT));

        assertDoesNotThrow(() -> transactionService.performTransaction(transactionModel));
    }

    @Test
    @DisplayName("should throw CardNotFoundException when card does not exist")
    void shouldThrowCardNotFoundExceptionWhenCardDoesNotExist() {
        when(cardRepository.findById(TestFixture.CARD_NUMBER)).thenReturn(Optional.empty());

        TransactionModel transactionModel = new TransactionModel(
                TestFixture.CARD_NUMBER,
                TestFixture.CARD_PASSWORD,
                new BigDecimal(TestFixture.CARD_AMOUNT));

        assertThrows(CardException.CardNotFoundException.class,
                () -> transactionService.performTransaction(transactionModel));
    }

    @Test
    @DisplayName("should throw BalanceException when card balance is insufficient")
    void shouldThrowBalanceExceptionWhenCardBalanceIsInsufficient() {
        Card card = TestFixture.createCard();
        card.setPassword(TestFixture.CARD_PASSWORD_HASH);
        when(cardRepository.findById(TestFixture.CARD_NUMBER)).thenReturn(Optional.of(card));

        TransactionModel transactionModel = new TransactionModel(
                TestFixture.CARD_NUMBER,
                TestFixture.CARD_PASSWORD,
                new BigDecimal(TestFixture.CARD_AMOUNT_HIGH));

        assertThrows(BalanceException.class,
                () -> transactionService.performTransaction(transactionModel));
    }

    @Test
    @DisplayName("should throw PasswordException when card password is incorrect")
    void shouldThrowPasswordExceptionWhenCardPasswordIsIncorrect() {
        Card card = TestFixture.createCard();
        card.setPassword(TestFixture.CARD_PASSWORD);
        when(cardRepository.findById(TestFixture.CARD_NUMBER)).thenReturn(Optional.of(card));

        TransactionModel transactionModel = new TransactionModel(
                TestFixture.CARD_NUMBER,
                TestFixture.CARD_PASSWORD,
                new BigDecimal(TestFixture.CARD_AMOUNT_HIGH));

        assertThrows(PasswordException.class,
                () -> transactionService.performTransaction(transactionModel));
    }
}
