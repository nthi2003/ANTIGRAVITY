package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FinancialHealthScore {
    private final int score;
    private final String status;
    private final BigDecimal totalBalance;
    private final boolean isAtRisk;
}
