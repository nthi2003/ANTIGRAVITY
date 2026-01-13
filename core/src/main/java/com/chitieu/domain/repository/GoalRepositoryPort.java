package com.chitieu.domain.repository;

import com.chitieu.domain.model.Goal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chitieu.domain.model.GoalRole;

public interface GoalRepositoryPort {
    List<Goal> findByUserId(UUID userId);

    Optional<Goal> findById(UUID id);

    Goal save(Goal goal, UUID ownerId);

    void addMember(UUID goalId, UUID userId, java.math.BigDecimal targetAmount, GoalRole role);

    void updateContribution(UUID goalId, UUID userId, java.math.BigDecimal amount);

    void deductAmount(UUID goalId, java.math.BigDecimal amount);
}
