package com.chitieu.persistence.repository;

import com.chitieu.persistence.entity.PrivacySettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrivacySettingsRepository extends JpaRepository<PrivacySettingsEntity, UUID> {
    Optional<PrivacySettingsEntity> findByUserId(@Param("userId") UUID userId);

    void deleteByUserId(@Param("userId") UUID userId);
}
