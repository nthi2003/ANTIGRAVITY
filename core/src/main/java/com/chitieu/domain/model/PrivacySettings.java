package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class PrivacySettings {
    private final UUID id;
    private final UUID userId;
    private final Visibility profileVisibility;
    private final Visibility goalsVisibility;
    private final Visibility transactionsVisibility;
    private final boolean allowFriendRequests;
    private final boolean showOnlineStatus;

    public boolean canViewProfile(UUID viewerId, boolean isFriend) {
        return switch (profileVisibility) {
            case PUBLIC -> true;
            case FRIENDS -> isFriend;
            case PRIVATE -> viewerId.equals(userId);
        };
    }

    public boolean canViewGoals(UUID viewerId, boolean isFriend) {
        return switch (goalsVisibility) {
            case PUBLIC -> true;
            case FRIENDS -> isFriend;
            case PRIVATE -> viewerId.equals(userId);
        };
    }

    public boolean canViewTransactions(UUID viewerId, boolean isFriend) {
        return switch (transactionsVisibility) {
            case PUBLIC -> true;
            case FRIENDS -> isFriend;
            case PRIVATE -> viewerId.equals(userId);
        };
    }
}
