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
import java.time.YearMonth;
import java.time.ZoneOffset;
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

/** Verifies reward aggregation, customer validation, and the rolling three-month window. */
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

    /** Rejects unknown customers without querying their transactions. */
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

    /** Aggregates per-transaction rewards by month and totals them for one customer. */
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

    /** Applies earning thresholds independently to each purchase. */
    @Test
    void calculateRewards_appliesThresholdsToEachTransactionIndividually() {
        Customer customer = customer(10L);
        when(customerRepository.existsById(10L)).thenReturn(true);
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                10L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(
                        transaction(customer, "60.00", 7, 5),
                        transaction(customer, "60.00", 7, 10)));

        CustomerRewards result = rewardsService.calculateRewards(10L);

        assertEquals(20, result.totalRewards());
        assertEquals(new MonthlyReward(YearMonth.of(2026, 7), 20),
                result.monthlyRewards().get(1));
    }

    /** Includes the current month and preceding two months across a year boundary. */
    @Test
    void calculateRewards_usesThreeMonthWindowAcrossYearBoundary() {
        Clock januaryClock = Clock.fixed(
                Instant.parse("2027-01-15T12:00:00Z"), ZoneOffset.UTC);
        RewardsService januaryService = new RewardsService(customerRepository, transactionRepository,
                new RewardPointsCalculator(), januaryClock);
        when(customerRepository.existsById(14L)).thenReturn(true);
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                14L, LocalDate.of(2026, 11, 1), LocalDate.of(2027, 1, 31)))
                .thenReturn(List.of());

        CustomerRewards result = januaryService.calculateRewards(14L);

        assertEquals(List.of(
                new MonthlyReward(YearMonth.of(2026, 11), 0),
                new MonthlyReward(YearMonth.of(2026, 12), 0),
                new MonthlyReward(YearMonth.of(2027, 1), 0)), result.monthlyRewards());
        verify(transactionRepository).findByCustomerIdAndTransactionDateBetween(
                14L, LocalDate.of(2026, 11, 1), LocalDate.of(2027, 1, 31));
    }

    /** Sums multiple transactions within the same month. */
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

    /** Places rewards in the month each transaction occurred. */
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

    /** Returns all three months with zero points when there are no transactions. */
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

    /** Returns summaries for every customer, including customers without transactions. */
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

    /**
     * Creates a customer fixture with a known identifier.
     *
     * @param id customer ID
     * @return customer fixture
     */
    private static Customer customer(long id) {
        Customer customer = new Customer("Test", "Customer");
        customer.setId(id);
        return customer;
    }

    /**
     * Creates a transaction fixture for the fixed test year.
     *
     * @param customer customer associated with the transaction
     * @param amount transaction amount
     * @param month transaction month
     * @param day transaction day of month
     * @return transaction fixture
     */
    private static Transaction transaction(Customer customer, String amount, int month, int day) {
        return new Transaction(customer, new BigDecimal(amount), LocalDate.of(2026, month, day));
    }
}
