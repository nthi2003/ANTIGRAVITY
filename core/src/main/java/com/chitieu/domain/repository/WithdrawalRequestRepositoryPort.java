package com.chitieu.domain.repository;

import com.chitieu.domain.model.WithdrawalRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WithdrawalRequestRepositoryPort {
    WithdrawalRequest save(WithdrawalRequest request);

    Optional<WithdrawalRequest> findById(UUID id);

    List<WithdrawalRequest> findByGoalId(UUID goalId);
}
