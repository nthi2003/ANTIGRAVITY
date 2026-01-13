package com.chitieu.domain.service;

import com.chitieu.domain.model.*;
import com.chitieu.domain.repository.AccountRepositoryPort;
import com.chitieu.domain.repository.DebtRepositoryPort;
import com.chitieu.domain.repository.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialHealthAssessmentService {

    private final AccountRepositoryPort accountRepository;
    private final DebtRepositoryPort debtRepository;
    private final TransactionRepositoryPort transactionRepository;

    private static final List<String> ESSENTIAL_CATEGORIES = Arrays.asList(
            "Food", "Housing", "Transportation", "Healthcare", "Utilities");

    private static final List<String> WANTS_CATEGORIES = Arrays.asList(
            "Entertainment", "Shopping", "Dining", "Travel", "Hobbies");

    /**
     * Calculate comprehensive financial health metrics
     */
    public FinancialHealthMetrics calculateFinancialHealth(UUID userId) {
        log.info("Calculating financial health for user: {}", userId);

        // Calculate all metrics
        NetWorthMetrics netWorth = calculateNetWorth(userId);
        LiquidityMetrics liquidity = calculateLiquidity(userId);
        BudgetRuleMetrics budgetRule = calculate503020Rule(userId);
        DebtMetrics debtMetrics = calculateDebtToIncome(userId);
        FinancialFreedomMetrics freedom = calculateFinancialFreedom(userId);

        // Calculate overall score (weighted average)
        int overallScore = calculateOverallScore(netWorth, liquidity, budgetRule, debtMetrics, freedom);

        return FinancialHealthMetrics.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .calculatedAt(LocalDate.now())
                // Net Worth
                .netWorth(netWorth.netWorth)
                .totalAssets(netWorth.totalAssets)
                .totalLiabilities(netWorth.totalLiabilities)
                .netWorthRating(netWorth.rating)
                // Liquidity
                .liquidAssets(liquidity.liquidAssets)
                .monthlyEssentialExpenses(liquidity.monthlyEssentialExpenses)
                .liquidityMonths(liquidity.liquidityMonths)
                .liquiditySafetyLevel(liquidity.safetyLevel)
                // Budget Rule
                .needsAmount(budgetRule.needsAmount)
                .needsPercent(budgetRule.needsPercent)
                .wantsAmount(budgetRule.wantsAmount)
                .wantsPercent(budgetRule.wantsPercent)
                .savingsAmount(budgetRule.savingsAmount)
                .savingsPercent(budgetRule.savingsPercent)
                .budgetCompliance(budgetRule.compliance)
                // Debt
                .monthlyIncome(debtMetrics.monthlyIncome)
                .monthlyDebtPayments(debtMetrics.monthlyDebtPayments)
                .debtToIncomeRatio(debtMetrics.dtiRatio)
                .debtRiskLevel(debtMetrics.riskLevel)
                // Financial Freedom
                .annualExpenses(freedom.annualExpenses)
                .financialFreedomNumber(freedom.fiNumber)
                .currentProgress(freedom.progressPercent)
                .yearsToFreedom(freedom.yearsToFI)
                .monthlyIncome(budgetRule.needsAmount.add(budgetRule.wantsAmount).add(budgetRule.savingsAmount)) // Total
                                                                                                                 // Income
                                                                                                                 // from
                                                                                                                 // 50/30/20
                                                                                                                 // rule
                .monthlyExpense(budgetRule.needsAmount.add(budgetRule.wantsAmount)) // Total Expense from 50/30/20 rule
                // Overall
                .overallScore(overallScore)
                .build();
    }

    /**
     * 1. Net Worth Calculator
     */
    private NetWorthMetrics calculateNetWorth(UUID userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        // Calculate total assets (positive balances)
        // Calculate total assets (positive balances)
        BigDecimal totalAssets = accounts.stream()
                .filter(a -> a.getBalance() != null && a.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total liabilities
        // 1. Credit card debt (negative balances)
        BigDecimal creditDebt = accounts.stream()
                .filter(a -> a.getType() == AccountType.CREDIT)
                .filter(a -> a.getBalance() != null && a.getBalance().compareTo(BigDecimal.ZERO) < 0)
                .map(a -> a.getBalance().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Loan debt (BORROW type, ACTIVE status)
        BigDecimal loanDebt = debtRepository.findByUserId(userId).stream()
                .filter(d -> d.getType() == DebtType.BORROW)
                .filter(d -> "ACTIVE".equals(d.getStatus()))
                .filter(d -> d.getAmount() != null)
                .map(Debt::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLiabilities = creditDebt.add(loanDebt);

        // Calculate net worth
        BigDecimal netWorth = totalAssets.subtract(totalLiabilities);

        // Determine rating
        NetWorthRating rating = determineNetWorthRating(netWorth, userId);

        return new NetWorthMetrics(netWorth, totalAssets, totalLiabilities, rating);
    }

    private NetWorthRating determineNetWorthRating(BigDecimal netWorth, UUID userId) {
        if (netWorth.compareTo(BigDecimal.ZERO) <= 0) {
            return NetWorthRating.POOR;
        }

        // TODO: Calculate YoY growth rate from historical data
        // For now, use simple thresholds
        BigDecimal growthRate = BigDecimal.ZERO; // Placeholder

        if (growthRate.compareTo(new BigDecimal("0.20")) > 0) {
            return NetWorthRating.EXCELLENT;
        } else if (growthRate.compareTo(new BigDecimal("0.05")) >= 0) {
            return NetWorthRating.GOOD;
        } else {
            return NetWorthRating.FAIR;
        }
    }

    /**
     * 2. Liquidity Ratio Calculator
     */
    private LiquidityMetrics calculateLiquidity(UUID userId) {
        // Calculate liquid assets (CASH, BANK, E_WALLET)
        BigDecimal liquidAssets = accountRepository.findByUserId(userId).stream()
                .filter(a -> a.getBalance() != null)
                .filter(a -> a.getType() == AccountType.CASH ||
                        a.getType() == AccountType.BANK ||
                        a.getType() == AccountType.E_WALLET)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate average monthly essential expenses (last 3 months)
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        BigDecimal totalEssentialExpenses = transactionRepository
                .findByUserId(userId).stream()
                .filter(t -> t.getDate() != null && t.getDate().isAfter(threeMonthsAgo))
                .filter(t -> "EXPENSE".equals(t.getType()))
                .filter(t -> t.getCategory() != null && ESSENTIAL_CATEGORIES.contains(t.getCategory()))
                .filter(t -> t.getAmount() != null)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyEssentialExpenses = totalEssentialExpenses.divide(
                new BigDecimal("3"), 2, RoundingMode.HALF_UP);

        // Calculate liquidity months
        BigDecimal liquidityMonths = monthlyEssentialExpenses.compareTo(BigDecimal.ZERO) > 0
                ? liquidAssets.divide(monthlyEssentialExpenses, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Determine safety level
        LiquiditySafetyLevel safetyLevel = determineSafetyLevel(liquidityMonths);

        return new LiquidityMetrics(liquidAssets, monthlyEssentialExpenses, liquidityMonths, safetyLevel);
    }

    private LiquiditySafetyLevel determineSafetyLevel(BigDecimal months) {
        if (months.compareTo(new BigDecimal("12")) > 0) {
            return LiquiditySafetyLevel.EXCELLENT;
        } else if (months.compareTo(new BigDecimal("6")) >= 0) {
            return LiquiditySafetyLevel.VERY_SAFE;
        } else if (months.compareTo(new BigDecimal("3")) >= 0) {
            return LiquiditySafetyLevel.SAFE;
        } else if (months.compareTo(new BigDecimal("1")) >= 0) {
            return LiquiditySafetyLevel.LOW;
        } else {
            return LiquiditySafetyLevel.CRITICAL;
        }
    }

    /**
     * 3. 50/30/20 Rule Calculator
     */
    private BudgetRuleMetrics calculate503020Rule(UUID userId) {
        // Get transactions from the last 30 days
        LocalDate lastMonth = LocalDate.now().minusMonths(1);

        List<Transaction> transactions = transactionRepository.findByUserId(userId).stream()
                .filter(t -> t.getDate() != null && (t.getDate().isAfter(lastMonth) || t.getDate().isEqual(lastMonth)))
                .filter(t -> t.getAmount() != null)
                .collect(Collectors.toList());

        // Calculate total income
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> "INCOME".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Categorize expenses
        BigDecimal needsExpense = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .filter(t -> t.getCategory() != null && ESSENTIAL_CATEGORIES.contains(t.getCategory()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal wantsExpense = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .filter(t -> t.getCategory() != null && WANTS_CATEGORIES.contains(t.getCategory()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Savings = Income - Needs - Wants
        BigDecimal savingsAmount = totalIncome.subtract(needsExpense).subtract(wantsExpense);

        // Calculate percentages
        BigDecimal needsPercent = calculatePercentage(needsExpense, totalIncome);
        BigDecimal wantsPercent = calculatePercentage(wantsExpense, totalIncome);
        BigDecimal savingsPercent = calculatePercentage(savingsAmount, totalIncome);

        // Determine compliance
        BudgetCompliance compliance = determineBudgetCompliance(needsPercent, wantsPercent, savingsPercent);

        return new BudgetRuleMetrics(
                needsExpense, needsPercent,
                wantsExpense, wantsPercent,
                savingsAmount, savingsPercent,
                compliance);
    }

    private BudgetCompliance determineBudgetCompliance(BigDecimal needs, BigDecimal wants, BigDecimal savings) {
        if (needs.compareTo(new BigDecimal("50")) <= 0 &&
                wants.compareTo(new BigDecimal("30")) <= 0 &&
                savings.compareTo(new BigDecimal("20")) >= 0) {
            return BudgetCompliance.EXCELLENT;
        } else if (needs.compareTo(new BigDecimal("60")) <= 0 &&
                savings.compareTo(new BigDecimal("15")) >= 0) {
            return BudgetCompliance.GOOD;
        } else if (needs.compareTo(new BigDecimal("70")) <= 0 &&
                savings.compareTo(new BigDecimal("10")) >= 0) {
            return BudgetCompliance.FAIR;
        } else {
            return BudgetCompliance.POOR;
        }
    }

    /**
     * 4. Debt-to-Income Ratio Calculator
     */
    private DebtMetrics calculateDebtToIncome(UUID userId) {
        // Calculate average monthly income (last 3 months)
        BigDecimal monthlyIncome = calculateAverageMonthlyIncome(userId, 3);

        // Calculate monthly debt payments
        // 1. Credit card minimum payments (assume 3% of balance)
        BigDecimal creditCardPayments = accountRepository.findByUserId(userId).stream()
                .filter(a -> a.getType() == AccountType.CREDIT)
                .filter(a -> a.getBalance() != null && a.getBalance().compareTo(BigDecimal.ZERO) < 0)
                .map(a -> a.getBalance().abs().multiply(new BigDecimal("0.03")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Loan payments (assume 1/12 of total for simplicity)
        BigDecimal loanPayments = debtRepository.findByUserId(userId).stream()
                .filter(d -> d.getType() == DebtType.BORROW)
                .filter(d -> "ACTIVE".equals(d.getStatus()))
                .filter(d -> d.getAmount() != null)
                .map(d -> d.getAmount().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMonthlyDebtPayments = creditCardPayments.add(loanPayments);

        // Calculate DTI ratio
        BigDecimal dtiRatio = monthlyIncome.compareTo(BigDecimal.ZERO) > 0
                ? totalMonthlyDebtPayments.divide(monthlyIncome, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        // Determine risk level
        DebtRiskLevel riskLevel = determineDebtRiskLevel(dtiRatio);

        return new DebtMetrics(monthlyIncome, totalMonthlyDebtPayments, dtiRatio, riskLevel);
    }

    private DebtRiskLevel determineDebtRiskLevel(BigDecimal dtiRatio) {
        if (dtiRatio.compareTo(new BigDecimal("20")) < 0) {
            return DebtRiskLevel.EXCELLENT;
        } else if (dtiRatio.compareTo(new BigDecimal("30")) < 0) {
            return DebtRiskLevel.GOOD;
        } else if (dtiRatio.compareTo(new BigDecimal("40")) < 0) {
            return DebtRiskLevel.MODERATE;
        } else if (dtiRatio.compareTo(new BigDecimal("50")) < 0) {
            return DebtRiskLevel.HIGH;
        } else {
            return DebtRiskLevel.CRITICAL;
        }
    }

    /**
     * 5. Financial Freedom Calculator (4% Rule)
     */
    private FinancialFreedomMetrics calculateFinancialFreedom(UUID userId) {
        // Calculate average monthly expenses (last 12 months)
        BigDecimal monthlyExpenses = calculateAverageMonthlyExpenses(userId, 12);
        BigDecimal annualExpenses = monthlyExpenses.multiply(new BigDecimal("12"));

        // Calculate FI number (4% rule = 25x annual expenses)
        BigDecimal fiNumber = annualExpenses.multiply(new BigDecimal("25"));

        // Get current net worth
        NetWorthMetrics netWorth = calculateNetWorth(userId);
        BigDecimal currentNetWorth = netWorth.netWorth;

        // Calculate progress percentage
        BigDecimal progressPercent = fiNumber.compareTo(BigDecimal.ZERO) > 0
                ? currentNetWorth.divide(fiNumber, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        // Calculate years to FI (assuming 10% annual return)
        BigDecimal monthlySavings = calculateMonthlySavings(userId);
        BigDecimal yearsToFI = calculateYearsToFI(currentNetWorth, fiNumber, monthlySavings, new BigDecimal("0.10"));

        return new FinancialFreedomMetrics(
                monthlyExpenses, annualExpenses, fiNumber,
                currentNetWorth, progressPercent, yearsToFI);
    }

    private BigDecimal calculateYearsToFI(BigDecimal current, BigDecimal target,
            BigDecimal monthlySavings, BigDecimal annualReturn) {
        if (monthlySavings.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("-1"); // Cannot reach FI without savings
        }

        if (current.compareTo(target) >= 0) {
            return BigDecimal.ZERO; // Already at FI
        }

        BigDecimal monthlyRate = annualReturn.divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);
        int months = 0;
        BigDecimal accumulated = current;

        while (accumulated.compareTo(target) < 0 && months < 1200) { // Max 100 years
            accumulated = accumulated.multiply(BigDecimal.ONE.add(monthlyRate))
                    .add(monthlySavings);
            months++;
        }

        return new BigDecimal(months).divide(new BigDecimal("12"), 1, RoundingMode.HALF_UP);
    }

    /**
     * Calculate overall health score (0-100)
     */
    private int calculateOverallScore(NetWorthMetrics netWorth, LiquidityMetrics liquidity,
            BudgetRuleMetrics budgetRule, DebtMetrics debt,
            FinancialFreedomMetrics freedom) {
        int netWorthScore = getRatingScore(netWorth.rating);
        int liquidityScore = getSafetyScore(liquidity.safetyLevel);
        int budgetScore = getComplianceScore(budgetRule.compliance);
        int debtScore = getDebtScore(debt.riskLevel);
        int freedomScore = Math.min(100, freedom.progressPercent.intValue());

        // Weighted average
        int totalScore = (netWorthScore * 25 + liquidityScore * 25 + budgetScore * 20 +
                debtScore * 20 + freedomScore * 10) / 100;

        return Math.max(0, Math.min(100, totalScore));
    }

    private int getRatingScore(NetWorthRating rating) {
        return switch (rating) {
            case EXCELLENT -> 100;
            case GOOD -> 75;
            case FAIR -> 50;
            case POOR -> 25;
        };
    }

    private int getSafetyScore(LiquiditySafetyLevel level) {
        return switch (level) {
            case EXCELLENT -> 100;
            case VERY_SAFE -> 85;
            case SAFE -> 70;
            case LOW -> 40;
            case CRITICAL -> 20;
        };
    }

    private int getComplianceScore(BudgetCompliance compliance) {
        return switch (compliance) {
            case EXCELLENT -> 100;
            case GOOD -> 75;
            case FAIR -> 50;
            case POOR -> 25;
        };
    }

    private int getDebtScore(DebtRiskLevel level) {
        return switch (level) {
            case EXCELLENT -> 100;
            case GOOD -> 80;
            case MODERATE -> 60;
            case HIGH -> 35;
            case CRITICAL -> 15;
        };
    }

    // Helper methods
    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return amount.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private BigDecimal calculateAverageMonthlyIncome(UUID userId, int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months);
        BigDecimal totalIncome = transactionRepository.findByUserId(userId).stream()
                .filter(t -> t.getDate() != null && t.getDate().isAfter(startDate))
                .filter(t -> "INCOME".equals(t.getType()))
                .filter(t -> t.getAmount() != null)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalIncome.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageMonthlyExpenses(UUID userId, int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months);
        BigDecimal totalExpenses = transactionRepository.findByUserId(userId).stream()
                .filter(t -> t.getDate() != null && t.getDate().isAfter(startDate))
                .filter(t -> "EXPENSE".equals(t.getType()))
                .filter(t -> t.getAmount() != null)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalExpenses.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMonthlySavings(UUID userId) {
        BigDecimal monthlyIncome = calculateAverageMonthlyIncome(userId, 3);
        BigDecimal monthlyExpenses = calculateAverageMonthlyExpenses(userId, 3);
        return monthlyIncome.subtract(monthlyExpenses);
    }

    // Inner classes for metrics
    private static class NetWorthMetrics {
        final BigDecimal netWorth;
        final BigDecimal totalAssets;
        final BigDecimal totalLiabilities;
        final NetWorthRating rating;

        NetWorthMetrics(BigDecimal netWorth, BigDecimal totalAssets, BigDecimal totalLiabilities,
                NetWorthRating rating) {
            this.netWorth = netWorth;
            this.totalAssets = totalAssets;
            this.totalLiabilities = totalLiabilities;
            this.rating = rating;
        }
    }

    private static class LiquidityMetrics {
        final BigDecimal liquidAssets;
        final BigDecimal monthlyEssentialExpenses;
        final BigDecimal liquidityMonths;
        final LiquiditySafetyLevel safetyLevel;

        LiquidityMetrics(BigDecimal liquidAssets, BigDecimal monthlyEssentialExpenses, BigDecimal liquidityMonths,
                LiquiditySafetyLevel safetyLevel) {
            this.liquidAssets = liquidAssets;
            this.monthlyEssentialExpenses = monthlyEssentialExpenses;
            this.liquidityMonths = liquidityMonths;
            this.safetyLevel = safetyLevel;
        }
    }

    private static class BudgetRuleMetrics {
        final BigDecimal needsAmount;
        final BigDecimal needsPercent;
        final BigDecimal wantsAmount;
        final BigDecimal wantsPercent;
        final BigDecimal savingsAmount;
        final BigDecimal savingsPercent;
        final BudgetCompliance compliance;

        BudgetRuleMetrics(BigDecimal needsAmount, BigDecimal needsPercent, BigDecimal wantsAmount,
                BigDecimal wantsPercent, BigDecimal savingsAmount, BigDecimal savingsPercent,
                BudgetCompliance compliance) {
            this.needsAmount = needsAmount;
            this.needsPercent = needsPercent;
            this.wantsAmount = wantsAmount;
            this.wantsPercent = wantsPercent;
            this.savingsAmount = savingsAmount;
            this.savingsPercent = savingsPercent;
            this.compliance = compliance;
        }
    }

    private static class DebtMetrics {
        final BigDecimal monthlyIncome;
        final BigDecimal monthlyDebtPayments;
        final BigDecimal dtiRatio;
        final DebtRiskLevel riskLevel;

        DebtMetrics(BigDecimal monthlyIncome, BigDecimal monthlyDebtPayments, BigDecimal dtiRatio,
                DebtRiskLevel riskLevel) {
            this.monthlyIncome = monthlyIncome;
            this.monthlyDebtPayments = monthlyDebtPayments;
            this.dtiRatio = dtiRatio;
            this.riskLevel = riskLevel;
        }
    }

    private static class FinancialFreedomMetrics {
        final BigDecimal monthlyExpenses;
        final BigDecimal annualExpenses;
        final BigDecimal fiNumber;
        final BigDecimal currentNetWorth;
        final BigDecimal progressPercent;
        final BigDecimal yearsToFI;

        FinancialFreedomMetrics(BigDecimal monthlyExpenses, BigDecimal annualExpenses, BigDecimal fiNumber,
                BigDecimal currentNetWorth, BigDecimal progressPercent, BigDecimal yearsToFI) {
            this.monthlyExpenses = monthlyExpenses;
            this.annualExpenses = annualExpenses;
            this.fiNumber = fiNumber;
            this.currentNetWorth = currentNetWorth;
            this.progressPercent = progressPercent;
            this.yearsToFI = yearsToFI;
        }
    }
}
