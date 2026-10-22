package com.rewards.config;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.rewards.model.Customer;
import com.rewards.model.Transaction;
import com.rewards.repository.CustomerRepository;
import com.rewards.repository.TransactionRepository;

/**
 * Loads a set of sample customer and transaction records when the application starts, used for
 * demonstrating the Rewards API.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    /**
     * Creates the initializer with the required repositories.
     *
     * @param customerRepository repository used to persist customer records
     * @param transactionRepository repository used to persist transaction records
     */
    public DataInitializer(CustomerRepository customerRepository, TransactionRepository transactionRepository, Clock clock) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    /**
     * Seeds the database with sample customers and transactions.
     *
     * @param args runtime arguments passed by Spring Boot
     */
    @Override
    public void run(String... args) {
        Customer anna = customerRepository.save(new Customer("Anna", "Smith"));
        Customer bob = customerRepository.save(new Customer("Bob", "Williams"));
        Customer chris = customerRepository.save(new Customer("Chris", "Johnson"));

        List<Transaction> transactions = new ArrayList<>();

        transactions.add(new Transaction(anna, new BigDecimal("15.99"), date(2, 5)));
        transactions.add(new Transaction(anna, new BigDecimal("10.50"), date(2, 12)));
        transactions.add(new Transaction(anna, new BigDecimal("120.00"), date(1, 3)));
        transactions.add(new Transaction(anna, new BigDecimal("49.97"), date(0, 9)));

        transactions.add(new Transaction(bob, new BigDecimal("50.00"), date(2, 14)));
        transactions.add(new Transaction(bob, new BigDecimal("26.75"), date(1, 7)));
        transactions.add(new Transaction(bob, new BigDecimal("63.25"), date(1, 22)));
        transactions.add(new Transaction(bob, new BigDecimal("155.95"), date(0, 16)));

        transactions.add(new Transaction(chris, new BigDecimal("97.80"), date(2, 9)));
        transactions.add(new Transaction(chris, new BigDecimal("9.77"), date(2, 20)));
        transactions.add(new Transaction(chris, new BigDecimal("100.00"), date(1, 20)));
        transactions.add(new Transaction(chris, new BigDecimal("100.01"), date(0, 28)));

        transactionRepository.saveAll(transactions);
    }

    /**
     * Creates a date for the seeded records using a fixed sample year.
     *
     * @param month target month for the transaction
     * @param dayOfMonth target day of month for the transaction
     * @return a LocalDate used in the seed dataset
     */
    private LocalDate date(int monthsAgo, int dayOfMonth) {
    return YearMonth.now(clock).minusMonths(monthsAgo).atDay(dayOfMonth);
}
}