package com.vr.miniauthorizer.controller;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.vr.miniauthorizer.exception.BalanceException;
import com.vr.miniauthorizer.model.TransactionModel;
import com.vr.miniauthorizer.service.TransactionService;
import com.vr.miniauthorizer.utils.ExceptionMessages;

import resources.fixtures.TestFixture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TransactionController")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    @DisplayName("should perform transaction and return 201 OK when authorization passes")
    void shouldPerformTransactionAndReturn201WhenAuthorizationPasses() throws Exception {
        doNothing().when(transactionService).performTransaction(any(TransactionModel.class));
        TransactionModel transaction = new TransactionModel(
                TestFixture.CARD_NUMBER,
                TestFixture.CARD_PASSWORD,
                new BigDecimal(TestFixture.CARD_AMOUNT));

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestFixture.writeJson(transaction)))
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));
    }

    @Test
    @DisplayName("should return 422 with SALDO_INSUFICIENTE when balance is insufficient")
    void shouldReturn422WithSaldoInsuficienteWhenBalanceIsInsufficient() throws Exception {
        doThrow(new BalanceException(ExceptionMessages.INSUFFICIENT_BALANCE))
                .when(transactionService).performTransaction(any(TransactionModel.class));

        TransactionModel transaction = new TransactionModel(
                TestFixture.CARD_NUMBER,
                TestFixture.CARD_PASSWORD,
                new BigDecimal(TestFixture.CARD_AMOUNT));

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestFixture.writeJson(transaction)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(ExceptionMessages.INSUFFICIENT_BALANCE));
    }
}
