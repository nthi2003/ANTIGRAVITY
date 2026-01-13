package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class FriendProfile {
    private final UUID userId;
    private final String username;
    private final String fullName;
    private final String avatar;
    private final String email;
    private final FriendshipStatus friendshipStatus;
    private final int mutualFriendsCount;
    private final int sharedGoalsCount;
    private final boolean isOnline;
    private final String rankTier; // BRONZE, SILVER, GOLD, PLATINUM, DIAMOND
}
