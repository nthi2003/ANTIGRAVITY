package com.chitieu.domain.repository;

import com.chitieu.domain.model.Debt;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DebtRepositoryPort {
    List<Debt> findByUserId(UUID userId);

    Debt save(Debt debt);

    Optional<Debt> findById(UUID id);
}
