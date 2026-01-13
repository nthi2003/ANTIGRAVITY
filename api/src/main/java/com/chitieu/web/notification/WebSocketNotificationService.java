package com.chitieu.web.notification;

import com.chitieu.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNotification(UUID userId, String title, String message, String type) {
        sendNotification(userId, title, message, type, new HashMap<>());
    }

    @Override
    public void sendNotification(UUID userId, String title, String message, String type, Map<String, String> metadata) {
        Map<String, String> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("message", message);
        payload.put("type", type);
        payload.put("timestamp", java.time.LocalDateTime.now().toString());

        // Add metadata (e.g., goalId, requestId)
        if (metadata != null) {
            payload.putAll(metadata);
        }

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                payload);
    }
}
