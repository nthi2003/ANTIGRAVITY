package com.chitieu.domain.repository;

import com.chitieu.domain.model.GoalInvitation;
import com.chitieu.domain.model.InvitationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalInvitationRepositoryPort {

    GoalInvitation save(GoalInvitation invitation);

    Optional<GoalInvitation> findById(UUID id);

    List<GoalInvitation> findByInvitedUserId(UUID userId);

    List<GoalInvitation> findByInvitedUserIdAndStatus(UUID userId, InvitationStatus status);

    List<GoalInvitation> findByGoalId(UUID goalId);

    boolean existsByGoalIdAndInvitedUserId(UUID goalId, UUID userId);

    void delete(UUID id);
}
