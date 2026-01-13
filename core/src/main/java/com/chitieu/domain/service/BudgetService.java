package com.chitieu.domain.service;

import com.chitieu.domain.model.Budget;
import com.chitieu.domain.model.Transaction;
import com.chitieu.domain.repository.BudgetRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetRepositoryPort budgetRepository;

    public void updateAndCheckBudgets(UUID userId, Transaction transaction) {
        if (!"EXPENSE".equals(transaction.getType())) {
            return;
        }

        List<Budget> budgets = budgetRepository.findByUserId(userId);
        budgets.stream()
                .filter(b -> b.getCategory().equals(transaction.getCategory()))
                .findFirst()
                .ifPresent(budget -> {
                    BigDecimal newSpent = budget.getSpentAmount().add(transaction.getAmount());

                    // Business Rule: Alerts at 50%, 80%, 100%
                    checkThresholds(budget, newSpent);

                    Budget updatedBudget = Budget.builder()
                            .id(budget.getId())
                            .category(budget.getCategory())
                            .limitAmount(budget.getLimitAmount())
                            .spentAmount(newSpent)
                            .userId(userId)
                            .build();

                    budgetRepository.save(updatedBudget);
                });
    }

    private void checkThresholds(Budget budget, BigDecimal newSpent) {
        BigDecimal limit = budget.getLimitAmount();
        if (limit.compareTo(BigDecimal.ZERO) <= 0)
            return;

        BigDecimal oldPercentage = budget.getSpentAmount().divide(limit, 2, RoundingMode.HALF_UP);
        BigDecimal newPercentage = newSpent.divide(limit, 2, RoundingMode.HALF_UP);

        checkAndNotify(budget.getCategory(), newPercentage, oldPercentage, new BigDecimal("0.50"), "50%");
        checkAndNotify(budget.getCategory(), newPercentage, oldPercentage, new BigDecimal("0.80"), "80%");
        checkAndNotify(budget.getCategory(), newPercentage, oldPercentage, new BigDecimal("1.00"), "100%");
    }

    private void checkAndNotify(String category, BigDecimal newPct, BigDecimal oldPct, BigDecimal threshold,
            String label) {
        if (newPct.compareTo(threshold) >= 0 && oldPct.compareTo(threshold) < 0) {
            log.warn("BUDGET_ALERT: Category {} reached {} of budget!", category, label);
            // In a real app, this would trigger a notification service (Push, Email, etc.)
        }
    }
}
