package com.rewards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Transaction details with reward points calculated for that individual purchase.
 *
 * @param id transaction ID
 * @param customerId ID of the customer who made the transaction
 * @param amount transaction amount
 * @param transactionDate date of the transaction
 * @param rewardPoints points earned on this transaction
 */
public record TransactionResponse(Long id, Long customerId, BigDecimal amount,
                                  LocalDate transactionDate, int rewardPoints) {}