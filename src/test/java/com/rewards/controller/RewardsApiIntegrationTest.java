package com.rewards.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Checks seeded data through the running Spring MVC application context. */
@SpringBootTest
@AutoConfigureMockMvc
class RewardsApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /** Confirms seeded customers and reward totals remain consistent. */
    @Test
    void seededCustomersHaveExpectedRewards() throws Exception {
        mockMvc.perform(get("/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].totalRewards", containsInAnyOrder(90, 173, 147)));
    }

    /** Confirms seeded customers and transaction rewards remain consistent. */
    @Test
    void seededCustomersHaveExpectedTransactions() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)))
                .andExpect(jsonPath("$[?(@.customerId == 1 && @.amount == 120.0)].rewardPoints",
                        containsInAnyOrder(90)));
    }
}
