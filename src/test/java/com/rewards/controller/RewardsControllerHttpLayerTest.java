package com.rewards.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RewardsControllerHttpLayerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllRewards_returnsThreeMonthSummariesForEveryCustomer() throws Exception {
        mockMvc.perform(get("/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].monthlyRewards", everyItem(hasSize(3))))
                .andExpect(jsonPath("$[*].totalRewards", containsInAnyOrder(90, 173, 147)));
    }

    @Test
    void getRewards_returnsThreeMonthlyValuesForValidCustomerId() throws Exception {
        YearMonth currentMonth = YearMonth.now();

        mockMvc.perform(get("/rewards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.monthlyRewards", hasSize(3)))
                .andExpect(jsonPath("$.monthlyRewards[0].month")
                        .value(currentMonth.minusMonths(2).toString()))
                .andExpect(jsonPath("$.monthlyRewards[0].points").value(0))
                .andExpect(jsonPath("$.monthlyRewards[1].month")
                        .value(currentMonth.minusMonths(1).toString()))
                .andExpect(jsonPath("$.monthlyRewards[1].points").value(90))
                .andExpect(jsonPath("$.monthlyRewards[2].month").value(currentMonth.toString()))
                .andExpect(jsonPath("$.monthlyRewards[2].points").value(0))
                .andExpect(jsonPath("$.totalRewards").value(90));
    }

    @Test
    void getRewards_returns404WithErrorJsonForNonexistentCustomer() throws Exception {
        mockMvc.perform(get("/rewards/45"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found: 45"));
    }

    @Test
    void getRewards_returns400WithErrorJsonForInvalidCustomerId() throws Exception {
        mockMvc.perform(get("/rewards/q"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Customer ID must be a number"));
    }

    @Test
    void getTransactions_returnsTransactionsWithCalculatedRewardPoints() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)))
                .andExpect(jsonPath("$[?(@.customerId == 1 && @.amount == 120.0)].rewardPoints",
                        containsInAnyOrder(90)));
    }
}
