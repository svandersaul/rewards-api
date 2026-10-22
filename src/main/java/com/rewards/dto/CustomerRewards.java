package com.rewards.dto;

import java.util.List;

public record CustomerRewards(Long customerId, List<MonthlyReward> monthlyRewards, int totalRewards) {}