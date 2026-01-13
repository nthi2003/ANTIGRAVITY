package com.chitieu.web.controller;

import com.chitieu.persistence.repository.AccountRepository;
import com.chitieu.persistence.repository.WealthProjection;
import com.chitieu.web.dto.LeaderboardEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final AccountRepository accountRepository;

    @GetMapping
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard() {
        // Get top 50 users
        List<WealthProjection> topUsers = accountRepository.findTopWealthyUsers(PageRequest.of(0, 50));

        List<LeaderboardEntry> leaderboard = topUsers.stream()
                .map(this::mapToEntry)
                .collect(Collectors.toList());

        return ResponseEntity.ok(leaderboard);
    }

    private LeaderboardEntry mapToEntry(WealthProjection projection) {
        BigDecimal wealth = projection.getTotalWealth();
        String tier = determineTier(wealth);

        return LeaderboardEntry.builder()
                .userId(projection.getUserId())
                .fullName(projection.getFullName())
                .username(projection.getUsername())
                .totalWealth(wealth)
                .rankTier(tier)
                .build();
    }

    private String determineTier(BigDecimal wealth) {
        if (wealth == null)
            return "BRONZE";

        long value = wealth.longValue();
        if (value >= 1_000_000_000)
            return "DIAMOND";
        if (value >= 200_000_000)
            return "PLATINUM";
        if (value >= 50_000_000)
            return "GOLD";
        if (value >= 10_000_000)
            return "SILVER";
        return "BRONZE";
    }
}
