package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class FinancialHealthMetrics {
    private final UUID id;
    private final UUID userId;
    private final LocalDate calculatedAt;

    // Net Worth
    private final BigDecimal netWorth;
    private final BigDecimal totalAssets;
    private final BigDecimal totalLiabilities;
    private final NetWorthRating netWorthRating;

    // Liquidity
    private final BigDecimal liquidAssets;
    private final BigDecimal monthlyEssentialExpenses;
    private final BigDecimal liquidityMonths;
    private final LiquiditySafetyLevel liquiditySafetyLevel;

    // Budget Rule 50/30/20
    private final BigDecimal needsAmount;
    private final BigDecimal needsPercent;
    private final BigDecimal wantsAmount;
    private final BigDecimal wantsPercent;
    private final BigDecimal savingsAmount;
    private final BigDecimal savingsPercent;
    private final BudgetCompliance budgetCompliance;

    // Debt to Income
    private final BigDecimal monthlyIncome;
    private final BigDecimal monthlyDebtPayments;
    private final BigDecimal debtToIncomeRatio;
    private final DebtRiskLevel debtRiskLevel;

    // Financial Freedom
    private final BigDecimal annualExpenses;
    private final BigDecimal financialFreedomNumber;
    private final BigDecimal currentProgress;
    private final BigDecimal yearsToFreedom;
    private final BigDecimal monthlyExpense;

    // Overall Score
    private final int overallScore; // 0-100
}
