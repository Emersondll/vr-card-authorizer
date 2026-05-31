package com.vr.miniauthorizer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.miniauthorizer.exception.CardException;
import com.vr.miniauthorizer.model.CardModel;
import com.vr.miniauthorizer.service.CardService;
import com.vr.miniauthorizer.utils.ExceptionMessages;
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
import resources.fixtures.TestFixture;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(cardController).build();
    }

    @Test
    @DisplayName("Test createCard Success")
    void testCreateCardSuccess() throws Exception {
        CardModel card = new CardModel(TestFixture.CARD_PASSWORD, TestFixture.CARD_NUMBER);

        when(cardService.createCard(any(CardModel.class))).thenReturn(card);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(card);

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCartao").value(TestFixture.CARD_NUMBER))
                .andExpect(jsonPath("$.senha").value(TestFixture.CARD_PASSWORD));
    }

    @Test
    @DisplayName("Test createCard Failure (Card Already Exists)")
    void testCreateCardFailure() throws Exception {
        CardModel card = new CardModel(TestFixture.CARD_NUMBER, TestFixture.CARD_PASSWORD);

        when(cardService.createCard(any(CardModel.class))).thenThrow(new CardException.CardAlreadyExistsException(card));

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(card);

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Test checkBalance Success")
    void testCheckBalanceSuccess() throws Exception {
        BigDecimal balance = new BigDecimal(TestFixture.CARD_AMOUNT);

        when(cardService.checkBalance(TestFixture.CARD_NUMBER)).thenReturn(balance);

        mockMvc.perform(get("/cartoes/{cardNumber}", TestFixture.CARD_NUMBER))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(TestFixture.CARD_AMOUNT));
    }

    @Test
    @DisplayName("Test checkBalance Not Found (CardNotFoundException)")
    void testCheckBalanceNotFound() throws Exception {
        when(cardService.checkBalance(TestFixture.CARD_NUMBER)).thenThrow(new CardException.CardNotFoundException(ExceptionMessages.CARD_NOT_FOUND));

        mockMvc.perform(get("/cartoes/{cardNumber}", TestFixture.CARD_NUMBER))
                .andExpect(status().isNotFound());
    }
}
