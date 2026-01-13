package com.chitieu.web.controller;

import com.chitieu.domain.model.FinancialHealthMetrics;
import com.chitieu.domain.service.FinancialHealthAssessmentService;
import com.chitieu.web.dto.FinancialHealthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financial-health")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class FinancialHealthController {

    private final FinancialHealthAssessmentService assessmentService;

    /**
     * Get comprehensive financial health score
     */
    @GetMapping({ "", "/" })
    public ResponseEntity<FinancialHealthResponse> getFinancialHealthScore(Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            log.info("Getting financial health score for user: {}", userId);

            FinancialHealthMetrics metrics = assessmentService.calculateFinancialHealth(userId);
            FinancialHealthResponse response = mapToResponse(metrics);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating financial health", e);
            // Return a safe default response to prevent frontend crash
            return ResponseEntity.ok(FinancialHealthResponse.builder()
                    .userId(getUserIdFromAuth(authentication).toString())
                    .calculatedAt(java.time.LocalDate.now())
                    .overallScore(0)
                    .score(0)
                    .status("UNKNOWN")
                    .recommendations(java.util.Collections
                            .singletonList("Không thể tính toán dữ liệu. Vui lòng kiểm tra lại giao dịch."))
                    .build());
        }
    }

    /**
     * Get net worth details
     */
    @GetMapping("/net-worth")
    public ResponseEntity<FinancialHealthResponse.NetWorthData> getNetWorth(Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        FinancialHealthMetrics metrics = assessmentService.calculateFinancialHealth(userId);

        FinancialHealthResponse.NetWorthData netWorth = FinancialHealthResponse.NetWorthData.builder()
                .value(metrics.getNetWorth())
                .totalAssets(metrics.getTotalAssets())
                .totalLiabilities(metrics.getTotalLiabilities())
                .rating(metrics.getNetWorthRating().name())
                .trend("STABLE") // TODO: Calculate from historical data
                .build();

        return ResponseEntity.ok(netWorth);
    }

    /**
     * Get liquidity ratio
     */
    @GetMapping("/liquidity")
    public ResponseEntity<FinancialHealthResponse.LiquidityData> getLiquidity(Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        FinancialHealthMetrics metrics = assessmentService.calculateFinancialHealth(userId);

        String message = generateLiquidityMessage(metrics.getLiquiditySafetyLevel());

        FinancialHealthResponse.LiquidityData liquidity = FinancialHealthResponse.LiquidityData.builder()
                .liquidAssets(metrics.getLiquidAssets())
                .monthlyEssentialExpenses(metrics.getMonthlyEssentialExpenses())
                .months(metrics.getLiquidityMonths())
                .safetyLevel(metrics.getLiquiditySafetyLevel().name())
                .message(message)
                .build();

        return ResponseEntity.ok(liquidity);
    }

    /**
     * Get 50/30/20 budget analysis
     */
    @GetMapping("/budget-rule")
    public ResponseEntity<FinancialHealthResponse.BudgetRuleData> getBudgetRule(Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        FinancialHealthMetrics metrics = assessmentService.calculateFinancialHealth(userId);

        String message = generateBudgetMessage(metrics.getBudgetCompliance());

        FinancialHealthResponse.BudgetRuleData budgetRule = FinancialHealthResponse.BudgetRuleData.builder()
                .needsAmount(metrics.getNeedsAmount())
                .needsPercent(metrics.getNeedsPercent())
                .wantsAmount(metrics.getWantsAmount())
                .wantsPercent(metrics.getWantsPercent())
                .savingsAmount(metrics.getSavingsAmount())
                .savingsPercent(metrics.getSavingsPercent())
                .compliance(metrics.getBudgetCompliance().name())
                .message(message)
                .build();

        return ResponseEntity.ok(budgetRule);
    }

    /**
     * Get debt-to-income ratio
     */
    @GetMapping("/debt-ratio")
    public ResponseEntity<FinancialHealthResponse.DebtData> getDebtRatio(Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        FinancialHealthMetrics metrics = assessmentService.calculateFinancialHealth(userId);

        String message = generateDebtMessage(metrics.getDebtRiskLevel());

        FinancialHealthResponse.DebtData debt = FinancialHealthResponse.DebtData.builder()
                .monthlyIncome(metrics.getMonthlyIncome())
                .monthlyDebtPayments(metrics.getMonthlyDebtPayments())
                .ratio(metrics.getDebtToIncomeRatio())
                .riskLevel(metrics.getDebtRiskLevel().name())
                .message(message)
                .build();

        return ResponseEntity.ok(debt);
    }

    /**
     * Get financial freedom calculator
     */
    @GetMapping("/fi-calculator")
    public ResponseEntity<FinancialHealthResponse.FinancialFreedomData> getFinancialFreedom(
            Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        FinancialHealthMetrics metrics = assessmentService.calculateFinancialHealth(userId);

        String message = generateFreedomMessage(metrics.getYearsToFreedom());

        FinancialHealthResponse.FinancialFreedomData freedom = FinancialHealthResponse.FinancialFreedomData.builder()
                .monthlyExpenses(metrics.getAnnualExpenses().divide(java.math.BigDecimal.valueOf(12), 2,
                        java.math.RoundingMode.HALF_UP))
                .annualExpenses(metrics.getAnnualExpenses())
                .targetAmount(metrics.getFinancialFreedomNumber())
                .currentAmount(metrics.getNetWorth())
                .progressPercent(metrics.getCurrentProgress())
                .yearsToFreedom(metrics.getYearsToFreedom())
                .message(message)
                .build();

        return ResponseEntity.ok(freedom);
    }

    /**
     * Get comprehensive monthly report
     */
    @GetMapping("/report")
    public ResponseEntity<FinancialHealthResponse> getMonthlyReport(Authentication authentication) {
        return getFinancialHealthScore(authentication);
    }

    // Helper methods
    private UUID getUserIdFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof com.chitieu.web.security.UserPrincipal) {
            return ((com.chitieu.web.security.UserPrincipal) authentication.getPrincipal()).getId();
        }
        // Fallback for testing/dev
        log.warn("Authentication principal is not UserPrincipal: {}",
                authentication != null ? authentication.getPrincipal() : "null");
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    private FinancialHealthResponse mapToResponse(FinancialHealthMetrics metrics) {
        int score = metrics.getOverallScore();
        String status = score >= 80 ? "EXCELLENT"
                : score >= 60 ? "GOOD" : score >= 40 ? "FAIR" : score >= 20 ? "POOR" : "CRITICAL";

        return FinancialHealthResponse.builder()
                .userId(metrics.getUserId().toString())
                .calculatedAt(metrics.getCalculatedAt())
                .overallScore(metrics.getOverallScore())
                .score(score) // Frontend compatibility
                .status(status)
                .totalBalance(metrics.getNetWorth())
                .monthlyIncome(metrics.getMonthlyIncome())
                .monthlyExpense(metrics.getMonthlyExpense())
                .isAtRisk(score < 50)
                .netWorth(FinancialHealthResponse.NetWorthData.builder()
                        .value(metrics.getNetWorth())
                        .totalAssets(metrics.getTotalAssets())
                        .totalLiabilities(metrics.getTotalLiabilities())
                        .rating(metrics.getNetWorthRating().name())
                        .trend("STABLE")
                        .build())
                .liquidity(FinancialHealthResponse.LiquidityData.builder()
                        .liquidAssets(metrics.getLiquidAssets())
                        .monthlyEssentialExpenses(metrics.getMonthlyEssentialExpenses())
                        .months(metrics.getLiquidityMonths())
                        .safetyLevel(metrics.getLiquiditySafetyLevel().name())
                        .message(generateLiquidityMessage(metrics.getLiquiditySafetyLevel()))
                        .build())
                .budgetRule(FinancialHealthResponse.BudgetRuleData.builder()
                        .needsAmount(metrics.getNeedsAmount())
                        .needsPercent(metrics.getNeedsPercent())
                        .wantsAmount(metrics.getWantsAmount())
                        .wantsPercent(metrics.getWantsPercent())
                        .savingsAmount(metrics.getSavingsAmount())
                        .savingsPercent(metrics.getSavingsPercent())
                        .compliance(metrics.getBudgetCompliance().name())
                        .message(generateBudgetMessage(metrics.getBudgetCompliance()))
                        .build())
                .debt(FinancialHealthResponse.DebtData.builder()
                        .monthlyIncome(metrics.getMonthlyIncome())
                        .monthlyDebtPayments(metrics.getMonthlyDebtPayments())
                        .ratio(metrics.getDebtToIncomeRatio())
                        .riskLevel(metrics.getDebtRiskLevel().name())
                        .message(generateDebtMessage(metrics.getDebtRiskLevel()))
                        .build())
                .financialFreedom(FinancialHealthResponse.FinancialFreedomData.builder()
                        .monthlyExpenses(metrics.getAnnualExpenses().divide(java.math.BigDecimal.valueOf(12), 2,
                                java.math.RoundingMode.HALF_UP))
                        .annualExpenses(metrics.getAnnualExpenses())
                        .targetAmount(metrics.getFinancialFreedomNumber())
                        .currentAmount(metrics.getNetWorth())
                        .progressPercent(metrics.getCurrentProgress())
                        .yearsToFreedom(metrics.getYearsToFreedom())
                        .message(generateFreedomMessage(metrics.getYearsToFreedom()))
                        .build())
                .recommendations(generateRecommendations(metrics))
                .build();
    }

    private String generateLiquidityMessage(com.chitieu.domain.model.LiquiditySafetyLevel level) {
        return switch (level) {
            case EXCELLENT -> "Xuất sắc! Bạn có quỹ dự phòng rất tốt, đủ để sống hơn 1 năm.";
            case VERY_SAFE -> "Rất an toàn! Quỹ dự phòng của bạn đủ cho 6-12 tháng.";
            case SAFE -> "An toàn! Bạn có quỹ dự phòng đủ cho 3-6 tháng.";
            case LOW -> "Cảnh báo! Quỹ dự phòng chỉ đủ cho 1-3 tháng. Nên tăng lên.";
            case CRITICAL -> "Nguy hiểm! Quỹ dự phòng dưới 1 tháng. Cần xây dựng ngay!";
        };
    }

    private String generateBudgetMessage(com.chitieu.domain.model.BudgetCompliance compliance) {
        return switch (compliance) {
            case EXCELLENT -> "Hoàn hảo! Bạn đang tuân thủ quy tắc 50/30/20 rất tốt.";
            case GOOD -> "Tốt! Ngân sách của bạn khá hợp lý, tiếp tục duy trì.";
            case FAIR -> "Chấp nhận được. Cần cải thiện tỷ lệ tiết kiệm.";
            case POOR -> "Cần cải thiện! Chi tiêu thiết yếu quá cao hoặc tiết kiệm quá thấp.";
        };
    }

    private String generateDebtMessage(com.chitieu.domain.model.DebtRiskLevel level) {
        return switch (level) {
            case EXCELLENT -> "Xuất sắc! Tỷ lệ nợ rất thấp, bạn đang quản lý nợ rất tốt.";
            case GOOD -> "Tốt! Tỷ lệ nợ ở mức an toàn.";
            case MODERATE -> "Trung bình. Cần cân nhắc trước khi vay thêm.";
            case HIGH -> "Cao! Nên ưu tiên trả nợ trước khi vay thêm.";
            case CRITICAL -> "Nguy hiểm! Bạn đang sống dựa vào nợ. Cần tái cấu trúc tài chính ngay!";
        };
    }

    private String generateFreedomMessage(java.math.BigDecimal years) {
        if (years.compareTo(java.math.BigDecimal.ZERO) < 0) {
            return "Bạn cần tăng thu nhập hoặc giảm chi tiêu để có thể đạt tự do tài chính.";
        } else if (years.compareTo(java.math.BigDecimal.ZERO) == 0) {
            return "Chúc mừng! Bạn đã đạt tự do tài chính!";
        } else if (years.compareTo(java.math.BigDecimal.valueOf(5)) < 0) {
            return String.format("Tuyệt vời! Bạn sẽ đạt tự do tài chính trong %.1f năm nữa!", years.doubleValue());
        } else if (years.compareTo(java.math.BigDecimal.valueOf(15)) < 0) {
            return String.format("Bạn đang trên đường đạt tự do tài chính trong %.1f năm.", years.doubleValue());
        } else {
            return String.format("Cần %.1f năm để đạt tự do tài chính. Hãy tăng tỷ lệ tiết kiệm!", years.doubleValue());
        }
    }

    private List<String> generateRecommendations(FinancialHealthMetrics metrics) {
        List<String> recommendations = new ArrayList<>();

        // Net Worth recommendations
        if (metrics.getNetWorthRating() == com.chitieu.domain.model.NetWorthRating.POOR) {
            recommendations.add("Tài sản ròng của bạn đang âm. Ưu tiên trả nợ và tăng tài sản.");
        }

        // Liquidity recommendations
        if (metrics.getLiquiditySafetyLevel() == com.chitieu.domain.model.LiquiditySafetyLevel.CRITICAL ||
                metrics.getLiquiditySafetyLevel() == com.chitieu.domain.model.LiquiditySafetyLevel.LOW) {
            recommendations.add("Xây dựng quỹ dự phòng ít nhất 3-6 tháng chi tiêu thiết yếu.");
        }

        // Budget recommendations
        if (metrics.getBudgetCompliance() == com.chitieu.domain.model.BudgetCompliance.POOR) {
            if (metrics.getNeedsPercent().compareTo(java.math.BigDecimal.valueOf(70)) > 0) {
                recommendations.add("Chi tiêu thiết yếu quá cao. Tìm cách giảm chi phí sinh hoạt.");
            }
            if (metrics.getSavingsPercent().compareTo(java.math.BigDecimal.valueOf(10)) < 0) {
                recommendations.add("Tỷ lệ tiết kiệm quá thấp. Mục tiêu tối thiểu là 20% thu nhập.");
            }
        }

        // Debt recommendations
        if (metrics.getDebtRiskLevel() == com.chitieu.domain.model.DebtRiskLevel.HIGH ||
                metrics.getDebtRiskLevel() == com.chitieu.domain.model.DebtRiskLevel.CRITICAL) {
            recommendations.add("Tỷ lệ nợ quá cao. Ưu tiên trả nợ lãi suất cao trước.");
        }

        // Financial Freedom recommendations
        if (metrics.getYearsToFreedom().compareTo(java.math.BigDecimal.valueOf(20)) > 0) {
            recommendations.add("Tăng tỷ lệ tiết kiệm và đầu tư để rút ngắn thời gian đạt tự do tài chính.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Tuyệt vời! Tài chính của bạn đang rất khỏe mạnh. Tiếp tục duy trì!");
        }

        return recommendations;
    }
}
