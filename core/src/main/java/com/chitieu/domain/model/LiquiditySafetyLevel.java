package com.chitieu.domain.model;

public enum LiquiditySafetyLevel {
    CRITICAL, // < 1 month
    LOW, // 1-3 months
    SAFE, // 3-6 months
    VERY_SAFE, // 6-12 months
    EXCELLENT // > 12 months
}
