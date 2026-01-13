package com.chitieu.domain.repository;

import com.chitieu.domain.model.PrivacySettings;

import java.util.Optional;
import java.util.UUID;

public interface PrivacySettingsRepositoryPort {

    PrivacySettings save(PrivacySettings settings);

    Optional<PrivacySettings> findByUserId(UUID userId);

    void delete(UUID userId);
}
