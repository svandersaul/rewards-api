package com.rewards.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

/** Calculates reward points for a single transaction amount. */
@Component
public class RewardPointsCalculator {
    private static final int LOWER_THRESHOLD_DOLLARS = 50;
    private static final int UPPER_THRESHOLD_DOLLARS = 100;
    private static final int POINTS_PER_DOLLAR_BETWEEN_THRESHOLDS = 1;
    private static final int POINTS_PER_DOLLAR_ABOVE_UPPER = 2;

    /**
     * Awards points above $50 and $100, counting whole dollars only.
     *
     * @param amount transaction amount
     * @return reward points earned for the transaction
     */
    public int pointsFor(BigDecimal amount) {
        int wholeDollars = amount.setScale(0, RoundingMode.DOWN).intValueExact();
        int dollarsBetween = Math.max(0, Math.min(wholeDollars, UPPER_THRESHOLD_DOLLARS) - LOWER_THRESHOLD_DOLLARS);
        int dollarsAbove = Math.max(0, wholeDollars - UPPER_THRESHOLD_DOLLARS);
        return dollarsBetween * POINTS_PER_DOLLAR_BETWEEN_THRESHOLDS
             + dollarsAbove * POINTS_PER_DOLLAR_ABOVE_UPPER;
    }
}