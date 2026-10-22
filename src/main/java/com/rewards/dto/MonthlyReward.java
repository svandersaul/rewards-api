package com.rewards.dto;

import java.time.YearMonth;

public record MonthlyReward(YearMonth month, int points) {}