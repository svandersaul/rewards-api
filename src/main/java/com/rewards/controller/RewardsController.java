package com.rewards.controller;

import com.rewards.dto.CustomerRewards;
import com.rewards.service.RewardsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping
    public List<CustomerRewards> getAllRewards() {
        return rewardsService.calculateAllRewards();
    }

    /**
     * Retrieves the monthly and total reward points for a given customer.
     *
     * @param customerId ID of the customer
     * @return reward details for the customer
     */
    @GetMapping("/{customerId}")
    public CustomerRewards getRewards(@PathVariable("customerId") Long customerId) {
        return rewardsService.calculateRewards(customerId);
    }
}