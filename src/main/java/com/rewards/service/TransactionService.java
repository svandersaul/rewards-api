package com.rewards.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rewards.dto.TransactionResponse;
import com.rewards.repository.TransactionRepository;

/** Reads transaction data and maps it to API response records. */
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final RewardPointsCalculator calculator;
    
    public TransactionService(TransactionRepository transactionRepository, RewardPointsCalculator calculator) {
        this.transactionRepository = transactionRepository;
        this.calculator = calculator;
    }

    /**
     * Returns all transactions ordered by date with per-transaction reward points.
     *
     * @return transaction response records ordered by transaction date
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll() {
        return transactionRepository.findAll(Sort.by("transactionDate")).stream()
                .map(t -> new TransactionResponse(t.getId(), t.getCustomer().getId(),
                        t.getAmount(), t.getTransactionDate(), calculator.pointsFor(t.getAmount())))
                .toList();
    }
}