package com.chitieu.domain.repository;

import com.chitieu.domain.model.Budget;
import java.util.List;
import java.util.UUID;

public interface BudgetRepositoryPort {
    List<Budget> findByUserId(UUID userId);

    void save(Budget budget);
}
