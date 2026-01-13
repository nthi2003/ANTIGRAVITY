package com.chitieu.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {
    private UUID userId;
    private String fullName;
    private String username;
    private BigDecimal totalWealth;
    private String rankTier; // BRONZE, SILVER, GOLD, PLATINUM, DIAMOND
}
