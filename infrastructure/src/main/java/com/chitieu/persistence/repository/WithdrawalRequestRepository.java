package com.chitieu.persistence.repository;

import com.chitieu.persistence.entity.WithdrawalRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequestEntity, UUID> {
    List<WithdrawalRequestEntity> findByGoalId(UUID goalId);
}
