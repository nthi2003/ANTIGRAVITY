package com.chitieu.web.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class FinancialHealthResponse {
    private final String userId;
    private final LocalDate calculatedAt;
    private final int overallScore;
    private final int score; // Alias for overallScore (for frontend compatibility)
    private final String status; // EXCELLENT, GOOD, FAIR, POOR, CRITICAL
    private final BigDecimal totalBalance; // Net worth
    private final BigDecimal monthlyIncome;
    private final BigDecimal monthlyExpense;
    @com.fasterxml.jackson.annotation.JsonProperty("isAtRisk")
    private final boolean isAtRisk; // True if score < 50

    // Net Worth
    private final NetWorthData netWorth;

    // Liquidity
    private final LiquidityData liquidity;

    // Budget Rule
    private final BudgetRuleData budgetRule;

    // Debt
    private final DebtData debt;

    // Financial Freedom
    private final FinancialFreedomData financialFreedom;

    // Recommendations
    private final java.util.List<String> recommendations;

    @Getter
    @Builder
    public static class NetWorthData {
        private final BigDecimal value;
        private final BigDecimal totalAssets;
        private final BigDecimal totalLiabilities;
        private final String rating; // EXCELLENT, GOOD, FAIR, POOR
        private final String trend; // UP, DOWN, STABLE
    }

    @Getter
    @Builder
    public static class LiquidityData {
        private final BigDecimal liquidAssets;
        private final BigDecimal monthlyEssentialExpenses;
        private final BigDecimal months;
        private final String safetyLevel; // CRITICAL, LOW, SAFE, VERY_SAFE, EXCELLENT
        private final String message;
    }

    @Getter
    @Builder
    public static class BudgetRuleData {
        private final BigDecimal needsAmount;
        private final BigDecimal needsPercent;
        private final BigDecimal wantsAmount;
        private final BigDecimal wantsPercent;
        private final BigDecimal savingsAmount;
        private final BigDecimal savingsPercent;
        private final String compliance; // EXCELLENT, GOOD, FAIR, POOR
        private final String message;
    }

    @Getter
    @Builder
    public static class DebtData {
        private final BigDecimal monthlyIncome;
        private final BigDecimal monthlyDebtPayments;
        private final BigDecimal ratio;
        private final String riskLevel; // EXCELLENT, GOOD, MODERATE, HIGH, CRITICAL
        private final String message;
    }

    @Getter
    @Builder
    public static class FinancialFreedomData {
        private final BigDecimal monthlyExpenses;
        private final BigDecimal annualExpenses;
        private final BigDecimal targetAmount;
        private final BigDecimal currentAmount;
        private final BigDecimal progressPercent;
        private final BigDecimal yearsToFreedom;
        private final String message;
    }
}
