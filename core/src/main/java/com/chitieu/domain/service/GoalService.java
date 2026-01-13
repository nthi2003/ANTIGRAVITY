package com.chitieu.domain.service;

import com.chitieu.domain.model.Goal;
import java.util.List;
import java.util.UUID;

public interface GoalService {
    List<Goal> getUserGoals(UUID userId);

    void contribute(UUID goalId, UUID userId, java.math.BigDecimal amount);

    List<com.chitieu.domain.model.Settlement> calculateSettlements(UUID goalId);

    void requestWithdrawal(UUID goalId, UUID requesterId, java.math.BigDecimal amount, String description);

    void approveWithdrawal(UUID requestId, UUID userId, com.chitieu.domain.model.ApprovalStatus status);

    List<com.chitieu.domain.model.WithdrawalRequest> getWithdrawalRequests(UUID goalId);
}
