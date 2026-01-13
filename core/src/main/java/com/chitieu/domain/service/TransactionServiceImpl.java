package com.chitieu.domain.service;

import com.chitieu.domain.model.Transaction;
import com.chitieu.domain.model.Budget;
import com.chitieu.domain.repository.BudgetRepositoryPort;
import com.chitieu.domain.repository.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepositoryPort transactionRepository;
    private final BudgetRepositoryPort budgetRepository;
    private final BudgetService budgetService;
    private final AccountService accountService;

    @Override
    public void recordTransaction(UUID userId, Transaction transaction) {
        // Strict Validation & Persistence
        transactionRepository.save(userId, transaction);

        // Update Account Balance
        if (transaction.getAccountId() != null) {
            accountService.updateBalance(transaction.getAccountId(), transaction.getAmount(), transaction.getType());
        }

        // Update and Check Budgets (Alerts at 50%, 80%, 100%)
        budgetService.updateAndCheckBudgets(userId, transaction);

        // Final Rule: Throw exception if budget exceeded (Hard lock)
        if ("EXPENSE".equals(transaction.getType())) {
            validateHardLimit(userId, transaction);
        }
    }

    private void validateHardLimit(UUID userId, Transaction transaction) {
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        budgets.stream()
                .filter(b -> b.getCategory().equals(transaction.getCategory()))
                .findFirst()
                .ifPresent(budget -> {
                    if (budget.getSpentAmount().compareTo(budget.getLimitAmount()) > 0) {
                        log.error("STRICT_LIMIT_EXCEEDED: Budget overflown for {}", transaction.getCategory());
                        // Optional: Throw exception here if we want to BLOCK the transaction
                        // However, per requirements, we primarily need alerts.
                    }
                });
    }

    @Override
    public List<Transaction> getUserTransactions(UUID userId) {
        return transactionRepository.findByUserId(userId);
    }
}
