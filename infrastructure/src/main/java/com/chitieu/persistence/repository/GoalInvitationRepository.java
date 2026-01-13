package com.chitieu.persistence.repository;

import com.chitieu.domain.model.InvitationStatus;
import com.chitieu.persistence.entity.GoalInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoalInvitationRepository extends JpaRepository<GoalInvitationEntity, UUID> {

    List<GoalInvitationEntity> findByInvitedUserId(UUID invitedUserId);

    @Query("SELECT g FROM GoalInvitationEntity g WHERE g.invitedUserId = :userId AND g.status = :status")
    List<GoalInvitationEntity> findByInvitedUserIdAndStatus(@Param("userId") UUID userId,
            @Param("status") InvitationStatus status);

    List<GoalInvitationEntity> findByGoalId(UUID goalId);

    boolean existsByGoalIdAndInvitedUserId(UUID goalId, UUID invitedUserId);
}
