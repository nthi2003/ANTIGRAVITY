package com.chitieu.domain.service;

import java.util.UUID;

public interface NotificationService {
    void sendNotification(UUID userId, String title, String message, String type);

    void sendNotification(UUID userId, String title, String message, String type,
            java.util.Map<String, String> metadata);
}
