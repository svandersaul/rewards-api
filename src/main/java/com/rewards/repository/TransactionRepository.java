package com.rewards.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rewards.model.Transaction;

/**
 * Data access layer for transaction records.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds every transaction associated with the specified customer.
     *
     * @param customerId ID of the customer
     * @return matching transactions
     */
    List<Transaction> findByCustomerId(Long customerId);

    /**
     * Finds a customer's transactions within an inclusive date range.
     *
     * @param customerId ID of the customer
     * @param start first included transaction date
     * @param end last included transaction date
     * @return matching transactions
     */
    List<Transaction> findByCustomerIdAndTransactionDateBetween(Long customerId, LocalDate start, LocalDate end);

    /**
     * Finds all transactions within an inclusive date range.
     *
     * @param start first included transaction date
     * @param end last included transaction date
     * @return matching transactions
     */
    List<Transaction> findByTransactionDateBetween(LocalDate start, LocalDate end);
}
