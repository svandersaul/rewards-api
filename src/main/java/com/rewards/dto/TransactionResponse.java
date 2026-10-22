package com.rewards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(Long id, Long customerId, BigDecimal amount,
                                  LocalDate transactionDate, int rewardPoints) {}