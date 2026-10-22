package com.rewards.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.rewards.dto.TransactionResponse;
import com.rewards.exception.GlobalExceptionHandler;
import com.rewards.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TransactionController(transactionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getTransactions_returnsTransactionsWithCalculatedRewardPoints() throws Exception {
        when(transactionService.findAll()).thenReturn(List.of(
                new TransactionResponse(7L, 42L, new BigDecimal("120.00"),
                        LocalDate.of(2026, 8, 10), 90)));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].customerId").value(42))
                .andExpect(jsonPath("$[0].amount").value(120.0))
                .andExpect(jsonPath("$[0].transactionDate").value("2026-08-10"))
                .andExpect(jsonPath("$[0].rewardPoints").value(90));
    }

    @Test
    void unknownPath_returns404WithErrorJson() throws Exception {
        mockMvc.perform(get("/transactions/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void wrongHttpMethod_returns405WithErrorJson() throws Exception {
        mockMvc.perform(post("/transactions"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value("HTTP method not supported"));
    }
}
