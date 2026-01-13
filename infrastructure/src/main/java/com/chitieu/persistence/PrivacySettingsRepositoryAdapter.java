package com.chitieu.persistence;

import com.chitieu.domain.model.PrivacySettings;
import com.chitieu.domain.repository.PrivacySettingsRepositoryPort;
import com.chitieu.persistence.entity.PrivacySettingsEntity;
import com.chitieu.persistence.repository.PrivacySettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PrivacySettingsRepositoryAdapter implements PrivacySettingsRepositoryPort {

    private final PrivacySettingsRepository privacyRepository;

    @Override
    public PrivacySettings save(PrivacySettings settings) {
        PrivacySettingsEntity entity = toEntity(settings);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        return toDomain(privacyRepository.save(entity));
    }

    @Override
    public Optional<PrivacySettings> findByUserId(UUID userId) {
        return privacyRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public void delete(UUID userId) {
        privacyRepository.deleteByUserId(userId);
    }

    private PrivacySettings toDomain(PrivacySettingsEntity entity) {
        return PrivacySettings.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .profileVisibility(entity.getProfileVisibility())
                .goalsVisibility(entity.getGoalsVisibility())
                .transactionsVisibility(entity.getTransactionsVisibility())
                .allowFriendRequests(entity.isAllowFriendRequests())
                .showOnlineStatus(entity.isShowOnlineStatus())
                .build();
    }

    private PrivacySettingsEntity toEntity(PrivacySettings domain) {
        return PrivacySettingsEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .profileVisibility(domain.getProfileVisibility())
                .goalsVisibility(domain.getGoalsVisibility())
                .transactionsVisibility(domain.getTransactionsVisibility())
                .allowFriendRequests(domain.isAllowFriendRequests())
                .showOnlineStatus(domain.isShowOnlineStatus())
                .build();
    }
}
