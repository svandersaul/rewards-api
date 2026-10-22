package com.rewards.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rewards.dto.CustomerRewards;
import com.rewards.dto.MonthlyReward;
import com.rewards.exception.CustomerNotFoundException;
import com.rewards.model.Transaction;
import com.rewards.repository.CustomerRepository;
import com.rewards.repository.TransactionRepository;

/**
 * Service responsible for calculating customer reward points based on their transactions.
 */
@Service
public class RewardsService {

    private static final int REWARD_PERIOD_MONTHS = 3;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final RewardPointsCalculator calculator;
    private final Clock clock;
    
    public RewardsService(CustomerRepository customerRepository, TransactionRepository transactionRepository,
                          RewardPointsCalculator calculator, Clock clock) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.calculator = calculator;
        this.clock = clock;
    }

    /**
     * Calculates monthly and total reward points for a customer.
     *
     * @param customerId ID of the customer to calculate rewards for
     * @return reward summary with monthly breakdown and overall total
     * @throws CustomerNotFoundException when the customer does not exist
     */
    @Transactional(readOnly = true)
    public CustomerRewards calculateRewards(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        List<Transaction> transactions = transactionRepository
                .findByCustomerIdAndTransactionDateBetween(customerId, periodStart(), periodEnd());
        return toCustomerRewards(customerId, transactions);
    }

    /**
     * Calculates reward summaries for every customer in the current reward window.
     *
     * @return rewards for all customers
     */
    @Transactional(readOnly = true)
    public List<CustomerRewards> calculateAllRewards() {
        Map<Long, List<Transaction>> transactionsByCustomerId = transactionRepository
                .findByTransactionDateBetween(periodStart(), periodEnd()).stream()
                .collect(Collectors.groupingBy(t -> t.getCustomer().getId()));
        return customerRepository.findAll().stream()
                .map(c -> toCustomerRewards(c.getId(),
                        transactionsByCustomerId.getOrDefault(c.getId(), List.of())))
                .toList();
    }

    /**
     * Builds a three-month summary from the supplied transactions.
     *
     * @param customerId ID of the customer
     * @param transactions transactions in the reward period
     * @return the customer's monthly and total rewards
     */
    private CustomerRewards toCustomerRewards(Long customerId, List<Transaction> transactions) {
        Map<YearMonth, Integer> pointsByMonth = transactions.stream().collect(
                Collectors.groupingBy(t -> YearMonth.from(t.getTransactionDate()),
                        Collectors.summingInt(t -> calculator.pointsFor(t.getAmount()))));
        List<MonthlyReward> monthlyRewards = rewardPeriodMonths().stream()
                .map(month -> new MonthlyReward(month, pointsByMonth.getOrDefault(month, 0)))
                .toList();
        int totalRewards = monthlyRewards.stream().mapToInt(MonthlyReward::points).sum();
        return new CustomerRewards(customerId, monthlyRewards, totalRewards);
    }

    /**
     * Returns the current calendar month and its two preceding months.
     *
     * @return the three months in chronological order
     */
    private List<YearMonth> rewardPeriodMonths() {
        YearMonth currentMonth = YearMonth.now(clock);
        return IntStream.range(0, REWARD_PERIOD_MONTHS)
                .mapToObj(i -> currentMonth.minusMonths(REWARD_PERIOD_MONTHS - 1 - i))
                .toList();
    }

    /**
     * Returns the first day of the oldest month in the reward window.
     *
     * @return first date included in the reward period
     */
    private LocalDate periodStart() {
        return rewardPeriodMonths().get(0).atDay(1);
    }

    /**
     * Returns the last day of the current month.
     *
     * @return last date included in the reward period
     */
    private LocalDate periodEnd() {
        return YearMonth.now(clock).atEndOfMonth();
    }
}
