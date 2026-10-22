package com.rewards.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.rewards.dto.CustomerRewards;
import com.rewards.dto.MonthlyReward;
import com.rewards.dto.TransactionResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RewardsControllerHttpLayerTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void getAllRewards_returnsMonthlySummariesForEveryCustomer() {
        webTestClient.get().uri("/rewards")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerRewards.class)
                .value(rewards -> {
                    assertEquals(3, rewards.size());
                    assertTrue(rewards.stream().allMatch(reward -> reward.monthlyRewards().size() == 3));
                    assertEquals(List.of(90, 173, 147),
                            rewards.stream().map(CustomerRewards::totalRewards).toList());
                });
    }

    @Test
    void getRewards_returns200WithThreeMonthlyValuesForValidCustomerId() {
        webTestClient.get().uri("/rewards/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerRewards.class)
                .value(body -> {
                    assertNotNull(body);
                    assertEquals(1L, body.customerId());
                    assertEquals(List.of(
                            new MonthlyReward(YearMonth.now().minusMonths(2), 0),
                            new MonthlyReward(YearMonth.now().minusMonths(1), 90),
                            new MonthlyReward(YearMonth.now(), 0)), body.monthlyRewards());
                    assertEquals(90, body.totalRewards());
                });
    }

    @Test
    void getRewards_returns404WithErrorJsonForNonexistentCustomer() {
        webTestClient.get().uri("/rewards/45")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Customer not found: 45");
    }

    @Test
    void getRewards_returns400WithErrorJsonForInvalidCustomerId() {
        webTestClient.get().uri("/rewards/q")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").exists();
    }

    @Test
    void getTransactions_returnsTransactionsWithCalculatedRewardPoints() {
        webTestClient.get().uri("/transactions")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionResponse.class)
                .value(transactions -> {
                    assertEquals(12, transactions.size());
                    assertTrue(transactions.stream().allMatch(transaction ->
                            transaction.id() != null && transaction.customerId() != null
                                    && transaction.rewardPoints() >= 0));
                    assertTrue(transactions.stream().anyMatch(transaction ->
                            transaction.customerId() == 1L
                                    && transaction.amount().compareTo(new java.math.BigDecimal("120.00")) == 0
                                    && transaction.rewardPoints() == 90));
                });
    }
}
