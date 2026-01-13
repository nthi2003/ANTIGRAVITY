package com.chitieu.persistence.repository;

import com.chitieu.persistence.entity.DebtEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DebtRepository extends JpaRepository<DebtEntity, UUID> {
    List<DebtEntity> findByUserId(UUID userId);
}
