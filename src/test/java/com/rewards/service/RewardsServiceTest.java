package com.rewards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rewards.dto.CustomerRewards;
import com.rewards.dto.MonthlyReward;
import com.rewards.exception.CustomerNotFoundException;
import com.rewards.model.Customer;
import com.rewards.model.Transaction;
import com.rewards.repository.CustomerRepository;
import com.rewards.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class RewardsServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-15T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private RewardsService rewardsService;

    @BeforeEach
    void setUp() {
        rewardsService = new RewardsService(customerRepository, transactionRepository,
                new RewardPointsCalculator(), CLOCK);
    }

    @Test
    void calculateRewards_throwsWhenCustomerDoesNotExist() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class,
                () -> rewardsService.calculateRewards(99L));

        assertEquals("Customer not found: 99", exception.getMessage());
        verify(transactionRepository, never())
                .findByCustomerIdAndTransactionDateBetween(99L, LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 8, 31));
    }

    @Test
    void calculateRewards_returnsMonthlyAndTotalRewardsForCustomer() {
        Customer customer = customer(7L);
        when(customerRepository.existsById(7L)).thenReturn(true);
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                7L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(
                        transaction(customer, "100.75", 6, 10),
                        transaction(customer, "120.00", 6, 20),
                        transaction(customer, "40.00", 7, 5)));

        CustomerRewards result = rewardsService.calculateRewards(7L);

        assertEquals(7L, result.customerId());
        assertEquals(List.of(
                new MonthlyReward(YearMonth.of(2026, 6), 140),
                new MonthlyReward(YearMonth.of(2026, 7), 0),
                new MonthlyReward(YearMonth.of(2026, 8), 0)), result.monthlyRewards());
        assertEquals(140, result.totalRewards());
        verify(transactionRepository).findByCustomerIdAndTransactionDateBetween(
                7L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31));
    }

    @Test
    void calculateRewards_handlesRewardCalculationBoundaries() {
        Customer customer = customer(10L);
        when(customerRepository.existsById(10L)).thenReturn(true);
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                10L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(
                        transaction(customer, "50.00", 6, 1),
                        transaction(customer, "50.99", 6, 2),
                        transaction(customer, "100.00", 6, 3),
                        transaction(customer, "100.99", 6, 4)));

        CustomerRewards result = rewardsService.calculateRewards(10L);

        assertEquals(100, result.totalRewards());
        assertEquals(100, result.monthlyRewards().getFirst().points());
    }

    @Test
    void calculateRewards_aggregatesMultipleTransactionsInSingleMonth() {
        Customer customer = customer(11L);
        when(customerRepository.existsById(11L)).thenReturn(true);
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                11L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(
                        transaction(customer, "60.00", 7, 5),
                        transaction(customer, "75.00", 7, 10),
                        transaction(customer, "85.00", 7, 15),
                        transaction(customer, "150.00", 7, 25)));

        CustomerRewards result = rewardsService.calculateRewards(11L);

        assertEquals(220, result.totalRewards());
        assertEquals(new MonthlyReward(YearMonth.of(2026, 7), 220),
                result.monthlyRewards().get(1));
    }

    @Test
    void calculateRewards_aggregatesTransactionsAcrossMonths() {
        Customer customer = customer(12L);
        when(customerRepository.existsById(12L)).thenReturn(true);
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                12L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(
                        transaction(customer, "120.00", 6, 10),
                        transaction(customer, "80.00", 7, 15),
                        transaction(customer, "110.00", 8, 20)));

        CustomerRewards result = rewardsService.calculateRewards(12L);

        assertEquals(List.of(
                new MonthlyReward(YearMonth.of(2026, 6), 90),
                new MonthlyReward(YearMonth.of(2026, 7), 30),
                new MonthlyReward(YearMonth.of(2026, 8), 70)), result.monthlyRewards());
        assertEquals(190, result.totalRewards());
    }

    @Test
    void calculateRewards_returnsZeroForEachPeriodMonthWhenCustomerHasNoTransactions() {
        when(customerRepository.existsById(13L)).thenReturn(true);
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                13L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of());

        CustomerRewards result = rewardsService.calculateRewards(13L);

        assertEquals(List.of(
                new MonthlyReward(YearMonth.of(2026, 6), 0),
                new MonthlyReward(YearMonth.of(2026, 7), 0),
                new MonthlyReward(YearMonth.of(2026, 8), 0)), result.monthlyRewards());
        assertEquals(0, result.totalRewards());
    }

    @Test
    void calculateAllRewards_includesCustomersWithoutTransactions() {
        Customer withTransactions = customer(1L);
        Customer withoutTransactions = customer(2L);
        when(transactionRepository.findByTransactionDateBetween(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(transaction(withTransactions, "120.00", 8, 10)));
        when(customerRepository.findAll()).thenReturn(List.of(withTransactions, withoutTransactions));

        List<CustomerRewards> results = rewardsService.calculateAllRewards();

        assertEquals(List.of(
                new CustomerRewards(1L, List.of(
                        new MonthlyReward(YearMonth.of(2026, 6), 0),
                        new MonthlyReward(YearMonth.of(2026, 7), 0),
                        new MonthlyReward(YearMonth.of(2026, 8), 90)), 90),
                new CustomerRewards(2L, List.of(
                        new MonthlyReward(YearMonth.of(2026, 6), 0),
                        new MonthlyReward(YearMonth.of(2026, 7), 0),
                        new MonthlyReward(YearMonth.of(2026, 8), 0)), 0)), results);
    }

    private static Customer customer(long id) {
        Customer customer = new Customer("Test", "Customer");
        customer.setId(id);
        return customer;
    }

    private static Transaction transaction(Customer customer, String amount, int month, int day) {
        return new Transaction(customer, new BigDecimal(amount), LocalDate.of(2026, month, day));
    }
}
