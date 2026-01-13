package com.chitieu.persistence;

import com.chitieu.domain.model.Budget;
import com.chitieu.domain.repository.BudgetRepositoryPort;
import com.chitieu.persistence.entity.BudgetEntity;
import com.chitieu.persistence.entity.UserEntity;
import com.chitieu.persistence.repository.BudgetRepository;
import com.chitieu.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BudgetRepositoryAdapter implements BudgetRepositoryPort {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @Override
    public List<Budget> findByUserId(UUID userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Budget budget) {
        UserEntity user = userRepository.findById(budget.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BudgetEntity entity = BudgetEntity.builder()
                .id(budget.getId())
                .user(user)
                .category(budget.getCategory())
                .limitAmount(budget.getLimitAmount())
                .spentAmount(budget.getSpentAmount() != null ? budget.getSpentAmount() : java.math.BigDecimal.ZERO)
                .build();

        budgetRepository.save(entity);
    }

    private Budget mapToDomain(BudgetEntity entity) {
        return Budget.builder()
                .id(entity.getId())
                .category(entity.getCategory())
                .limitAmount(entity.getLimitAmount())
                .spentAmount(entity.getSpentAmount())
                .userId(entity.getUser().getId())
                .build();
    }
}
