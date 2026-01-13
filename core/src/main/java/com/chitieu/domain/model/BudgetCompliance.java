package com.chitieu.domain.model;

public enum BudgetCompliance {
    EXCELLENT, // Needs <= 50%, Wants <= 30%, Savings >= 20%
    GOOD, // Needs <= 60%, Savings >= 15%
    FAIR, // Needs <= 70%, Savings >= 10%
    POOR // Needs > 70% or Savings < 10%
}
