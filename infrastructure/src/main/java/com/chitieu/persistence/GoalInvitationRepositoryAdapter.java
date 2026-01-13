package com.chitieu.persistence;

import com.chitieu.domain.model.GoalInvitation;
import com.chitieu.domain.model.InvitationStatus;
import com.chitieu.domain.repository.GoalInvitationRepositoryPort;
import com.chitieu.persistence.entity.GoalEntity;
import com.chitieu.persistence.entity.GoalInvitationEntity;
import com.chitieu.persistence.entity.UserEntity;
import com.chitieu.persistence.repository.GoalInvitationRepository;
import com.chitieu.persistence.repository.GoalRepository;
import com.chitieu.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GoalInvitationRepositoryAdapter implements GoalInvitationRepositoryPort {

    private final GoalInvitationRepository goalInvitationRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    @Override
    public GoalInvitation save(GoalInvitation invitation) {
        GoalInvitationEntity entity = toEntity(invitation);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        return toDomain(goalInvitationRepository.save(entity));
    }

    @Override
    public Optional<GoalInvitation> findById(UUID id) {
        return goalInvitationRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<GoalInvitation> findByInvitedUserId(UUID userId) {
        return goalInvitationRepository.findByInvitedUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<GoalInvitation> findByInvitedUserIdAndStatus(UUID userId, InvitationStatus status) {
        return goalInvitationRepository.findByInvitedUserIdAndStatus(userId, status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<GoalInvitation> findByGoalId(UUID goalId) {
        return goalInvitationRepository.findByGoalId(goalId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByGoalIdAndInvitedUserId(UUID goalId, UUID userId) {
        return goalInvitationRepository.existsByGoalIdAndInvitedUserId(goalId, userId);
    }

    @Override
    public void delete(UUID id) {
        goalInvitationRepository.deleteById(id);
    }

    private GoalInvitation toDomain(GoalInvitationEntity entity) {
        String goalTitle = goalRepository.findById(entity.getGoalId())
                .map(GoalEntity::getTitle)
                .orElse("Unknown Goal");

        String invitedUserName = userRepository.findById(entity.getInvitedUserId())
                .map(UserEntity::getFullName)
                .orElse("Unknown User");

        String invitedByName = userRepository.findById(entity.getInvitedBy())
                .map(UserEntity::getFullName)
                .orElse("Unknown User");

        return GoalInvitation.builder()
                .id(entity.getId())
                .goalId(entity.getGoalId())
                .goalTitle(goalTitle)
                .invitedUserId(entity.getInvitedUserId())
                .invitedUserName(invitedUserName)
                .invitedBy(entity.getInvitedBy())
                .invitedByName(invitedByName)
                .role(entity.getRole())
                .status(entity.getStatus())
                .message(entity.getMessage())
                .invitedAt(entity.getInvitedAt())
                .respondedAt(entity.getRespondedAt())
                .build();
    }

    private GoalInvitationEntity toEntity(GoalInvitation domain) {
        return GoalInvitationEntity.builder()
                .id(domain.getId())
                .goalId(domain.getGoalId())
                .invitedUserId(domain.getInvitedUserId())
                .invitedBy(domain.getInvitedBy())
                .role(domain.getRole())
                .status(domain.getStatus())
                .message(domain.getMessage())
                .invitedAt(domain.getInvitedAt())
                .respondedAt(domain.getRespondedAt())
                .build();
    }
}
