package com.rewards.service;

import com.rewards.dto.TransactionResponse;
import com.rewards.repository.TransactionRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final RewardPointsCalculator calculator;
    
    public TransactionService(TransactionRepository transactionRepository, RewardPointsCalculator calculator) {
        this.transactionRepository = transactionRepository;
        this.calculator = calculator;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll() {
        return transactionRepository.findAll(Sort.by("transactionDate")).stream()
                .map(t -> new TransactionResponse(t.getId(), t.getCustomer().getId(),
                        t.getAmount(), t.getTransactionDate(), calculator.pointsFor(t.getAmount())))
                .toList();
    }
}