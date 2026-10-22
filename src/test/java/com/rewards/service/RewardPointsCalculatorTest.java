package com.rewards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Verifies reward point calculations at and around the earning thresholds. */
public class RewardPointsCalculatorTest {
    private final RewardPointsCalculator calculator = new RewardPointsCalculator();

    /**
     * Checks that each amount earns the expected number of points.
     *
     * @param amount transaction amount
     * @param expectedPoints expected reward points
     */
    @ParameterizedTest(name = "${0} earns {1} points")
    @CsvSource({"0.00,0", "50.00,0", "50.99,0", "51.00,1", "75.00,25", "99.99,49",
                "100.00,50", "100.99,50", "101.00,52", "120.00,90", "155.95,160"})
    void pointsFor_returnsExpectedPoints(BigDecimal amount, int expectedPoints) {
        assertEquals(expectedPoints, calculator.pointsFor(amount));
    }
}
