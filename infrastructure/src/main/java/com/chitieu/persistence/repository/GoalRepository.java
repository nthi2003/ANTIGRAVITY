package com.chitieu.persistence.repository;

import com.chitieu.persistence.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {

    @Query("SELECT DISTINCT g FROM GoalEntity g JOIN GoalMemberEntity gm ON g.id = gm.goal.id WHERE gm.user.id = :userId OR g.owner.id = :userId")
    List<GoalEntity> findAllByUserId(@Param("userId") UUID userId);
}
