package com.rewards.dto;

import java.util.List;

/**
 * Reward totals and monthly breakdown for one customer.
 *
 * @param customerId ID of the customer
 * @param monthlyRewards rewards for each month in the calculation period
 * @param totalRewards sum of the monthly reward points
 */
public record CustomerRewards(Long customerId, List<MonthlyReward> monthlyRewards, int totalRewards) {}