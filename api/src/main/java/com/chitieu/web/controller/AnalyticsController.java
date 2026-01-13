package com.chitieu.web.controller;

import com.chitieu.domain.service.AnalyticsService;
import com.chitieu.web.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SecurityUtils securityUtils;

    @GetMapping("/category")
    public ResponseEntity<Map<String, BigDecimal>> getSpendingByCategory() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getSpendingByCategory(userId));
    }

    @GetMapping("/suggestion")
    public ResponseEntity<String> getAISuggestion() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.generateAISuggestion(userId));
    }
}
