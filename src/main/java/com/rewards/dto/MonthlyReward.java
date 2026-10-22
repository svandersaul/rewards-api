package com.rewards.dto;

import java.time.YearMonth;

/**
 * Reward points earned by a customer during one calendar month.
 *
 * @param month calendar month for the reward total
 * @param points reward points earned during the month
 */
public record MonthlyReward(YearMonth month, int points) {}