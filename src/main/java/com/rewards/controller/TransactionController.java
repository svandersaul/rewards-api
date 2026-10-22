package com.rewards.controller;

import com.rewards.dto.TransactionResponse;
import com.rewards.service.TransactionService;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @GetMapping
    public List<TransactionResponse> getTransactions() {
        return transactionService.findAll();
    }
}