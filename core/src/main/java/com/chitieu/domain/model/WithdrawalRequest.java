package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class WithdrawalRequest {
    private final UUID id;
    private final UUID goalId; // The common fund
    private final UUID requesterId;
    private final String requesterName;
    private final BigDecimal amount;
    private final String description;
    private final ApprovalStatus status;
    private final LocalDateTime createdAt;
    private final List<Approval> approvals;

    @Getter
    @Builder
    public static class Approval {
        private final UUID userId;
        private final String userName;
        private final ApprovalStatus status;
        private final LocalDateTime updatedAt;
    }
}
