package com.rewards.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rewards.dto.TransactionResponse;
import com.rewards.service.TransactionService;

/** Exposes transaction data through the REST API. */
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Returns all transactions with their calculated reward points.
     *
     * @return all transactions with calculated reward points
     */
    @GetMapping
    public List<TransactionResponse> getTransactions() {
        return transactionService.findAll();
    }
}