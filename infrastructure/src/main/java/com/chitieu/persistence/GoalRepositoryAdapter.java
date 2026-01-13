package com.chitieu.persistence;

import com.chitieu.domain.model.Goal;
import com.chitieu.domain.model.GoalRole;
import com.chitieu.domain.repository.GoalRepositoryPort;
import com.chitieu.persistence.entity.GoalEntity;
import com.chitieu.persistence.entity.GoalMemberEntity;
import com.chitieu.persistence.entity.UserEntity;
import com.chitieu.persistence.repository.GoalMemberRepository;
import com.chitieu.persistence.repository.GoalRepository;
import com.chitieu.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GoalRepositoryAdapter implements GoalRepositoryPort {

        private final GoalRepository goalRepository;
        private final GoalMemberRepository goalMemberRepository;
        private final UserRepository userRepository;

        @Override
        public List<Goal> findByUserId(UUID userId) {
                return goalRepository.findAllByUserId(userId).stream()
                                .map(this::mapToDomain)
                                .collect(Collectors.toList());
        }

        @Override
        public Optional<Goal> findById(UUID id) {
                return goalRepository.findById(id).map(this::mapToDomain);
        }

        @Override
        @Transactional
        public Goal save(Goal goal, UUID ownerId) {
                UserEntity owner = userRepository.findById(ownerId)
                                .orElseThrow(() -> new RuntimeException("Owner not found"));

                GoalEntity entity = GoalEntity.builder()
                                .id(goal.getId())
                                .title(goal.getTitle())
                                .targetAmount(goal.getTargetAmount())
                                .currentAmount(goal.getCurrentAmount() != null ? goal.getCurrentAmount()
                                                : BigDecimal.ZERO)
                                .isLocked(goal.isLocked())
                                .owner(owner)
                                .build();

                GoalEntity saved = goalRepository.save(entity);

                // Add owner as a member with OWNER role
                addMember(saved.getId(), ownerId, BigDecimal.ZERO, GoalRole.OWNER);

                return mapToDomain(saved);
        }

        @Override
        public void addMember(UUID goalId, UUID userId, BigDecimal targetAmount, GoalRole role) {
                GoalEntity goal = goalRepository.findById(goalId)
                                .orElseThrow(() -> new RuntimeException("Goal not found"));
                UserEntity user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                GoalMemberEntity member = GoalMemberEntity.builder()
                                .goal(goal)
                                .user(user)
                                .contributedAmount(BigDecimal.ZERO)
                                .targetAmount(targetAmount)
                                .role(role)
                                .build();

                goalMemberRepository.save(member);
        }

        @Override
        @Transactional
        public void updateContribution(UUID goalId, UUID userId, BigDecimal amount) {
                GoalEntity goal = goalRepository.findById(goalId)
                                .orElseThrow(() -> new RuntimeException("Goal not found"));
                GoalMemberEntity member = goalMemberRepository.findByGoalIdAndUserId(goalId, userId)
                                .orElseThrow(() -> new RuntimeException("Member not found in this goal"));

                member.setContributedAmount(member.getContributedAmount().add(amount));
                goal.setCurrentAmount(goal.getCurrentAmount().add(amount));

                goalMemberRepository.save(member);
                goalRepository.save(goal);
        }

        @Override
        @Transactional
        public void deductAmount(UUID goalId, BigDecimal amount) {
                GoalEntity goal = goalRepository.findById(goalId)
                                .orElseThrow(() -> new RuntimeException("Goal not found"));
                goal.setCurrentAmount(goal.getCurrentAmount().subtract(amount));
                goalRepository.save(goal);
        }

        private Goal mapToDomain(GoalEntity entity) {
                List<GoalMemberEntity> members = goalMemberRepository.findByGoalId(entity.getId());
                return Goal.builder()
                                .id(entity.getId())
                                .title(entity.getTitle())
                                .targetAmount(entity.getTargetAmount())
                                .currentAmount(entity.getCurrentAmount())
                                .isLocked(entity.isLocked())
                                .members(members.stream()
                                                .map(m -> Goal.GoalMember.builder()
                                                                .userId(m.getUser().getId())
                                                                .userName(m.getUser().getFullName())
                                                                .contributedAmount(m.getContributedAmount())
                                                                .targetAmount(m.getTargetAmount())
                                                                .role(m.getRole())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build();
        }
}
