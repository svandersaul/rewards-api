package com.rewards.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rewards.dto.CustomerRewards;
import com.rewards.dto.MonthlyReward;
import com.rewards.exception.CustomerNotFoundException;
import com.rewards.service.RewardsService;

@ExtendWith(MockitoExtension.class)
class RewardsControllerTest {

    @Mock
    private RewardsService rewardsService;

    @InjectMocks
    private RewardsController rewardsController;

    @Test
    void getAllRewards_returnsRewardSummariesForAllCustomers() {
        List<CustomerRewards> rewards = List.of(new CustomerRewards(
                42L, List.of(new MonthlyReward(java.time.YearMonth.of(2026, 7), 120)), 120));
        when(rewardsService.calculateAllRewards()).thenReturn(rewards);

        List<CustomerRewards> result = rewardsController.getAllRewards();

        assertSame(rewards, result);
        verify(rewardsService).calculateAllRewards();
    }

    @Test
    void getRewards_returnsRewardSummaryForExistingCustomer() {
        CustomerRewards rewards = new CustomerRewards(42L, List.of(), 0);
        when(rewardsService.calculateRewards(42L)).thenReturn(rewards);

        CustomerRewards result = rewardsController.getRewards(42L);

        assertSame(rewards, result);
        verify(rewardsService).calculateRewards(42L);
    }

    @Test
    void getRewards_throwsForMissingCustomer() {
        when(rewardsService.calculateRewards(99L))
                .thenThrow(new CustomerNotFoundException(99L));

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class,
                () -> rewardsController.getRewards(99L));

        assertEquals("Customer not found: 99", exception.getMessage());
        verify(rewardsService).calculateRewards(99L);
    }
}
