package com.chitieu.domain.service;

import com.chitieu.domain.model.*;
import com.chitieu.domain.repository.GoalInvitationRepositoryPort;
import com.chitieu.domain.repository.GoalRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalInvitationService {

    private final GoalInvitationRepositoryPort invitationRepository;
    private final GoalRepositoryPort goalRepository;
    private final FriendshipService friendshipService;

    @Transactional
    public GoalInvitation inviteFriendToGoal(UUID goalId, UUID invitedUserId, UUID invitedBy, GoalRole role,
            String message) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (!friendshipService.areFriends(invitedBy, invitedUserId)) {
            throw new RuntimeException("Can only invite friends");
        }

        if (invitationRepository.existsByGoalIdAndInvitedUserId(goalId, invitedUserId)) {
            throw new RuntimeException("Already invited");
        }

        GoalInvitation invitation = GoalInvitation.builder()
                .goalId(goalId)
                .goalTitle(goal.getTitle())
                .invitedUserId(invitedUserId)
                .invitedBy(invitedBy)
                .role(role)
                .status(InvitationStatus.PENDING)
                .message(message)
                .invitedAt(LocalDateTime.now())
                .build();

        return invitationRepository.save(invitation);
    }

    @Transactional
    public void acceptInvitation(UUID userId, UUID invitationId) {
        GoalInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!invitation.getInvitedUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (!invitation.isPending()) {
            throw new RuntimeException("Invalid invitation status");
        }

        GoalInvitation accepted = GoalInvitation.builder()
                .id(invitation.getId())
                .goalId(invitation.getGoalId())
                .goalTitle(invitation.getGoalTitle())
                .invitedUserId(invitation.getInvitedUserId())
                .invitedBy(invitation.getInvitedBy())
                .role(invitation.getRole())
                .status(InvitationStatus.ACCEPTED)
                .message(invitation.getMessage())
                .invitedAt(invitation.getInvitedAt())
                .respondedAt(LocalDateTime.now())
                .build();

        invitationRepository.save(accepted);
    }

    @Transactional
    public void declineInvitation(UUID userId, UUID invitationId) {
        GoalInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!invitation.getInvitedUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        GoalInvitation declined = GoalInvitation.builder()
                .id(invitation.getId())
                .goalId(invitation.getGoalId())
                .goalTitle(invitation.getGoalTitle())
                .invitedUserId(invitation.getInvitedUserId())
                .invitedBy(invitation.getInvitedBy())
                .role(invitation.getRole())
                .status(InvitationStatus.DECLINED)
                .message(invitation.getMessage())
                .invitedAt(invitation.getInvitedAt())
                .respondedAt(LocalDateTime.now())
                .build();

        invitationRepository.save(declined);
    }

    public List<GoalInvitation> getPendingInvitations(UUID userId) {
        return invitationRepository.findByInvitedUserIdAndStatus(userId, InvitationStatus.PENDING);
    }
}
