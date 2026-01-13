package com.chitieu.domain.service;

import com.chitieu.domain.model.PrivacySettings;
import com.chitieu.domain.model.Visibility;
import com.chitieu.domain.repository.PrivacySettingsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrivacyService {

    private final PrivacySettingsRepositoryPort privacyRepository;

    public PrivacySettings getSettings(UUID userId) {
        return privacyRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
    }

    @Transactional
    public PrivacySettings updateSettings(UUID userId, PrivacySettings settings) {
        return privacyRepository.save(settings);
    }

    private PrivacySettings createDefaultSettings(UUID userId) {
        return PrivacySettings.builder()
                .userId(userId)
                .profileVisibility(Visibility.FRIENDS)
                .goalsVisibility(Visibility.FRIENDS)
                .transactionsVisibility(Visibility.PRIVATE)
                .allowFriendRequests(true)
                .showOnlineStatus(true)
                .build();
    }
}
