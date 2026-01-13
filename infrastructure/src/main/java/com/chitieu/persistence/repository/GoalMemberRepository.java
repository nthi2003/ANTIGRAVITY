package com.chitieu.persistence.repository;

import com.chitieu.persistence.entity.GoalMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GoalMemberRepository extends JpaRepository<GoalMemberEntity, UUID> {
    List<GoalMemberEntity> findByGoalId(UUID goalId);

    java.util.Optional<GoalMemberEntity> findByGoalIdAndUserId(UUID goalId, UUID userId);
}
