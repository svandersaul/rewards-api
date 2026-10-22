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
     * Finds all transactions associated with a specific customer.
     *
     * @param customerId ID of the customer to retrieve transactions for
     * @return list of transactions for the specified customer
     */
    List<Transaction> findByCustomerId(Long customerId);

    List<Transaction> findByCustomerIdAndTransactionDateBetween(Long customerId, LocalDate start, LocalDate end);
    List<Transaction> findByTransactionDateBetween(LocalDate start, LocalDate end);
}
