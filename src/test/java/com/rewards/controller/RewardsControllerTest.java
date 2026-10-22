package com.rewards.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.rewards.dto.CustomerRewards;
import com.rewards.dto.MonthlyReward;
import com.rewards.exception.CustomerNotFoundException;
import com.rewards.exception.GlobalExceptionHandler;
import com.rewards.service.RewardsService;

/** Tests reward HTTP endpoints using standalone MockMvc and a mocked service. */
@ExtendWith(MockitoExtension.class)
class RewardsControllerTest {

    @Mock
    private RewardsService rewardsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RewardsController(rewardsService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /** Returns monthly reward summaries for each customer. */
    @Test
    void getAllRewards_returnsMonthlySummariesForEveryCustomer() throws Exception {
        when(rewardsService.calculateAllRewards()).thenReturn(List.of(
                new CustomerRewards(1L, List.of(
                        new MonthlyReward(YearMonth.of(2026, 6), 0),
                        new MonthlyReward(YearMonth.of(2026, 7), 90),
                        new MonthlyReward(YearMonth.of(2026, 8), 0)), 90),
                new CustomerRewards(2L, List.of(
                        new MonthlyReward(YearMonth.of(2026, 6), 0),
                        new MonthlyReward(YearMonth.of(2026, 7), 13),
                        new MonthlyReward(YearMonth.of(2026, 8), 160)), 173)));

        mockMvc.perform(get("/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].monthlyRewards", hasSize(3)))
                .andExpect(jsonPath("$[0].totalRewards").value(90))
                .andExpect(jsonPath("$[1].customerId").value(2))
                .andExpect(jsonPath("$[1].totalRewards").value(173));
    }

    /** Returns the requested customer's monthly and total rewards. */
    @Test
    void getRewards_returnsMonthlySummaryForCustomer() throws Exception {
        when(rewardsService.calculateRewards(42L)).thenReturn(new CustomerRewards(42L, List.of(
                new MonthlyReward(YearMonth.of(2026, 6), 0),
                new MonthlyReward(YearMonth.of(2026, 7), 90),
                new MonthlyReward(YearMonth.of(2026, 8), 0)), 90));

        mockMvc.perform(get("/rewards/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(42))
                .andExpect(jsonPath("$.monthlyRewards", hasSize(3)))
                .andExpect(jsonPath("$.monthlyRewards[1].month").value("2026-07"))
                .andExpect(jsonPath("$.monthlyRewards[1].points").value(90))
                .andExpect(jsonPath("$.totalRewards").value(90));
    }

    /** Returns a problem response when the customer ID is not numeric. */
    @Test
    void getRewards_returns400ForInvalidCustomerId() throws Exception {
        mockMvc.perform(get("/rewards/q"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Customer ID must be a number"));
    }

    /** Returns a not-found problem when the customer does not exist. */
    @Test
    void getRewards_returns404ForNonexistentCustomer() throws Exception {
        when(rewardsService.calculateRewards(45L)).thenThrow(new CustomerNotFoundException(45L));

        mockMvc.perform(get("/rewards/45"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Customer not found: 45"));
    }

    /** Returns 404 for paths that do not match an endpoint. */
    @Test
    void unknownPath_returns404NotServerError() throws Exception {
        mockMvc.perform(get("/nope"))
                .andExpect(status().isNotFound());
    }

    /** Returns 405 when an endpoint is called with an unsupported method. */
    @Test
    void wrongHttpMethod_returns405() throws Exception {
        mockMvc.perform(post("/rewards/1"))
                .andExpect(status().isMethodNotAllowed());
    }

}
