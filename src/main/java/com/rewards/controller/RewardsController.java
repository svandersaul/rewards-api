package com.rewards.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rewards.dto.CustomerRewards;
import com.rewards.service.RewardsService;

/**
 * REST controller that exposes reward point totals for customers.
 */
@RestController
@RequestMapping("/rewards")
public class RewardsController {

    private final RewardsService rewardsService;

    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    /**
     * Returns reward summaries for every customer.
     *
     * @return reward summaries for all customers
     */
    @GetMapping
    public List<CustomerRewards> getAllRewards() {
        return rewardsService.calculateAllRewards();
    }

    /**
     * Returns monthly and total reward points for the specified customer.
     *
     * @param customerId ID of the customer
     * @return the customer's monthly and total rewards
     */
    @GetMapping("/{customerId}")
    public CustomerRewards getRewards(@PathVariable("customerId") Long customerId) {
        return rewardsService.calculateRewards(customerId);
    }
}