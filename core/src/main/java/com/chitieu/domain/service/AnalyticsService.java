package com.chitieu.domain.service;

import com.chitieu.domain.model.Transaction;
import com.chitieu.domain.repository.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepositoryPort transactionRepository;

    public Map<String, BigDecimal> getSpendingByCategory(UUID userId) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));
    }

    public String generateAISuggestion(UUID userId) {
        // AI Logic Simulation
        Map<String, BigDecimal> spending = getSpendingByCategory(userId);
        if (spending.getOrDefault("Food", BigDecimal.ZERO).compareTo(new BigDecimal("5000000")) > 0) {
            return "Your Dining expense is high. Reducing eating out could save you 20% this month.";
        }
        return "Your financial health is stable. Keep tracking your daily transactions.";
    }
}
