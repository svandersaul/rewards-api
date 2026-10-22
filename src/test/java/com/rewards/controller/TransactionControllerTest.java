package com.rewards.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rewards.dto.TransactionResponse;
import com.rewards.service.TransactionService;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void getTransactions_returnsTransactionsFromService() {
        List<TransactionResponse> transactions = List.of();
        when(transactionService.findAll()).thenReturn(transactions);

        List<TransactionResponse> result = transactionController.getTransactions();

        assertSame(transactions, result);
        verify(transactionService).findAll();
    }
}
