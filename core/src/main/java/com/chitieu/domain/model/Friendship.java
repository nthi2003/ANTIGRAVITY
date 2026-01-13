package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class Friendship {
    private final UUID id;
    private final UUID userId;
    private final UUID friendId;
    private final FriendshipStatus status;
    private final UUID requestedBy;
    private final LocalDateTime requestedAt;
    private final LocalDateTime acceptedAt;

    public boolean isPending() {
        return status == FriendshipStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == FriendshipStatus.ACCEPTED;
    }

    public boolean isBlocked() {
        return status == FriendshipStatus.BLOCKED;
    }

    public boolean isRequestedBy(UUID userId) {
        return requestedBy.equals(userId);
    }
}
