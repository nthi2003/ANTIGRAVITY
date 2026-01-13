package com.chitieu.domain.service;

import com.chitieu.domain.model.Account;
import com.chitieu.domain.model.AccountType;
import com.chitieu.domain.model.FinancialHealthScore;
import com.chitieu.domain.model.Transaction;
import com.chitieu.domain.repository.AccountRepositoryPort;
import com.chitieu.domain.repository.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialHealthServiceImpl implements FinancialHealthService {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepositoryPort accountRepository;

    @Override
    public FinancialHealthScore calculateHealthScore(UUID userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        List<Account> accounts = accountRepository.findByUserId(userId);

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Savings Rate factor (40% weight)
        int savingsScore = calculateSavingsScore(transactions);

        // Debt Ratio factor (60% weight)
        int debtScore = calculateDebtScore(accounts);

        int finalScore = (int) (savingsScore * 0.4 + debtScore * 0.6);

        return FinancialHealthScore.builder()
                .score(finalScore)
                .totalBalance(totalBalance)
                .status(determineStatus(finalScore))
                .isAtRisk(finalScore < 40)
                .build();
    }

    private int calculateSavingsScore(List<Transaction> transactions) {
        BigDecimal income = transactions.stream()
                .filter(t -> "INCOME".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (income.compareTo(BigDecimal.ZERO) <= 0)
            return expense.compareTo(BigDecimal.ZERO) > 0 ? 0 : 70;

        BigDecimal savingsRate = income.subtract(expense).divide(income, 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        return Math.min(100, Math.max(0, savingsRate.intValue()));
    }

    private int calculateDebtScore(List<Account> accounts) {
        BigDecimal creditDebt = accounts.stream()
                .filter(a -> a.getType() == AccountType.CREDIT && a.getBalance().compareTo(BigDecimal.ZERO) < 0)
                .map(a -> a.getBalance().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAssets = accounts.stream()
                .filter(a -> a.getType() != AccountType.CREDIT)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAssets.compareTo(BigDecimal.ZERO) <= 0)
            return creditDebt.compareTo(BigDecimal.ZERO) > 0 ? 0 : 80;

        BigDecimal debtRatio = creditDebt.divide(totalAssets, 2, RoundingMode.HALF_UP);
        if (debtRatio.compareTo(new BigDecimal("0.30")) <= 0)
            return 100;
        if (debtRatio.compareTo(new BigDecimal("0.70")) <= 0)
            return 50;
        return 0;
    }

    private String determineStatus(int score) {
        if (score >= 80)
            return "EXCELLENT";
        if (score >= 50)
            return "STABLE";
        return "CRITICAL";
    }
}
