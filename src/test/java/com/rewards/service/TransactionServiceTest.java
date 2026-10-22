package com.rewards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import com.rewards.dto.TransactionResponse;
import com.rewards.model.Customer;
import com.rewards.model.Transaction;
import com.rewards.repository.TransactionRepository;

/** Verifies transaction response mapping and date ordering. */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    /** Includes transaction, customer, amount, date, and calculated points in the response. */
    @Test
    void findAll_returnsTransactionDetailsWithCalculatedPointsSortedByDate() {
        Customer customer = new Customer("Test", "Customer");
        ReflectionTestUtils.setField(customer, "id", 7L);
        Transaction transaction = new Transaction(
                customer, new BigDecimal("120.50"), LocalDate.of(2026, 8, 10));
        ReflectionTestUtils.setField(transaction, "id", 23L);
        when(transactionRepository.findAll(Sort.by("transactionDate")))
                .thenReturn(List.of(transaction));

        TransactionService transactionService = new TransactionService(
                transactionRepository, new RewardPointsCalculator());
        List<TransactionResponse> result = transactionService.findAll();

        assertEquals(List.of(new TransactionResponse(23L, 7L, new BigDecimal("120.50"),
                LocalDate.of(2026, 8, 10), 90)), result);
        verify(transactionRepository).findAll(Sort.by("transactionDate"));
    }
}
