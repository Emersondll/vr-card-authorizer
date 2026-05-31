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
import com.vr.miniauthorizer.exception.CardException;
import com.vr.miniauthorizer.model.CardModel;
import com.vr.miniauthorizer.repository.CardRepository;

import resources.fixtures.TestFixture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardServiceImpl")
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    private CardServiceImpl cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardServiceImpl(cardRepository, new BigDecimal(TestFixture.CARD_AMOUNT));
    }

    @Test
    @DisplayName("should create card successfully when card does not exist")
    void shouldCreateCardSuccessfullyWhenCardDoesNotExist() {
        CardModel cardModel = new CardModel(TestFixture.CARD_PASSWORD, TestFixture.CARD_NUMBER);
        Card savedCard = TestFixture.createCard();

        when(cardRepository.findById(TestFixture.CARD_NUMBER)).thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        CardModel created = cardService.createCard(cardModel);

        assertEquals(cardModel, created);
    }

    @Test
    @DisplayName("should throw CardAlreadyExistsException when card already exists")
    void shouldThrowCardAlreadyExistsExceptionWhenCardAlreadyExists() {
        Card existingCard = TestFixture.createCard();
        when(cardRepository.findById(TestFixture.CARD_NUMBER)).thenReturn(Optional.of(existingCard));

        CardModel cardModel = new CardModel(TestFixture.CARD_PASSWORD, TestFixture.CARD_NUMBER);

        assertThrows(CardException.CardAlreadyExistsException.class,
                () -> cardService.createCard(cardModel));
    }

    @Test
    @DisplayName("should return balance when card exists")
    void shouldReturnBalanceWhenCardExists() {
        Card card = TestFixture.createCard();
        when(cardRepository.findById(TestFixture.CARD_NUMBER)).thenReturn(Optional.of(card));

        BigDecimal balance = cardService.checkBalance(TestFixture.CARD_NUMBER);

        assertEquals(new BigDecimal(TestFixture.CARD_AMOUNT), balance);
    }

    @Test
    @DisplayName("should throw CardNotFoundException when card does not exist for balance check")
    void shouldThrowCardNotFoundExceptionWhenCardDoesNotExistForBalanceCheck() {
        when(cardRepository.findById(TestFixture.CARD_NUMBER)).thenReturn(Optional.empty());

        assertThrows(CardException.CardNotFoundException.class,
                () -> cardService.checkBalance(TestFixture.CARD_NUMBER));
    }
}
