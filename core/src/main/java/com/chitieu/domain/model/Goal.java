package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class Goal {
    private final UUID id;
    private final String title;
    private final BigDecimal targetAmount;
    private final BigDecimal currentAmount;
    private final boolean isLocked;
    private final List<GoalMember> members;

    @Getter
    @Builder
    public static class GoalMember {
        private final UUID userId;
        private final String userName;
        private final BigDecimal contributedAmount;
        private final BigDecimal targetAmount;
        private final GoalRole role;
    }
}
