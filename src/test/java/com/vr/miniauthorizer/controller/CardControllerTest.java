package com.vr.miniauthorizer.controller;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.miniauthorizer.exception.CardException;
import com.vr.miniauthorizer.exception.GlobalExceptionHandler;
import com.vr.miniauthorizer.model.CardModel;
import com.vr.miniauthorizer.service.CardService;
import com.vr.miniauthorizer.utils.ExceptionMessages;

import resources.fixtures.TestFixture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardController")
class CardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(cardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("should create card and return 201 when card does not exist")
    void shouldCreateCardAndReturn201WhenCardDoesNotExist() throws Exception {
        CardModel card = new CardModel(TestFixture.CARD_PASSWORD, TestFixture.CARD_NUMBER);
        when(cardService.createCard(any(CardModel.class))).thenReturn(card);

        String requestBody = new ObjectMapper().writeValueAsString(card);

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCartao").value(TestFixture.CARD_NUMBER))
                .andExpect(jsonPath("$.senha").value(TestFixture.CARD_PASSWORD));
    }

    @Test
    @DisplayName("should return 422 with card body when card already exists")
    void shouldReturn422WithCardBodyWhenCardAlreadyExists() throws Exception {
        CardModel card = new CardModel(TestFixture.CARD_PASSWORD, TestFixture.CARD_NUMBER);
        when(cardService.createCard(any(CardModel.class)))
                .thenThrow(new CardException.CardAlreadyExistsException(card));

        String requestBody = new ObjectMapper().writeValueAsString(card);

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.numeroCartao").value(TestFixture.CARD_NUMBER));
    }

    @Test
    @DisplayName("should return balance and 200 when card exists")
    void shouldReturnBalanceAnd200WhenCardExists() throws Exception {
        BigDecimal balance = new BigDecimal(TestFixture.CARD_AMOUNT);
        when(cardService.checkBalance(TestFixture.CARD_NUMBER)).thenReturn(balance);

        mockMvc.perform(get("/cartoes/{cardNumber}", TestFixture.CARD_NUMBER))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(TestFixture.CARD_AMOUNT));
    }

    @Test
    @DisplayName("should return 404 when card does not exist for balance check")
    void shouldReturn404WhenCardDoesNotExistForBalanceCheck() throws Exception {
        when(cardService.checkBalance(TestFixture.CARD_NUMBER))
                .thenThrow(new CardException.CardNotFoundException(ExceptionMessages.CARD_NOT_FOUND));

        mockMvc.perform(get("/cartoes/{cardNumber}", TestFixture.CARD_NUMBER))
                .andExpect(status().isNotFound());
    }
}
