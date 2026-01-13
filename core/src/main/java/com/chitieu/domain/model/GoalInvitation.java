package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class GoalInvitation {
    private final UUID id;
    private final UUID goalId;
    private final String goalTitle;
    private final UUID invitedUserId;
    private final String invitedUserName;
    private final UUID invitedBy;
    private final String invitedByName;
    private final GoalRole role;
    private final InvitationStatus status;
    private final String message;
    private final LocalDateTime invitedAt;
    private final LocalDateTime respondedAt;

    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == InvitationStatus.ACCEPTED;
    }

    public boolean isDeclined() {
        return status == InvitationStatus.DECLINED;
    }
}
