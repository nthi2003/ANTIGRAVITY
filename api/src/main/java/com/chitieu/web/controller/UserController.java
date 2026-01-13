package com.chitieu.web.controller;

import com.chitieu.domain.model.User;
import com.chitieu.domain.service.UserService;
import com.chitieu.web.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody User userUpdate) {
        return ResponseEntity.ok(userService.updateProfile(principal.getId(), userUpdate));
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchUsers(query));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<com.chitieu.web.dto.UserStatsResponse> getMyStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        com.chitieu.domain.model.UserStatistics stats = userService.getUserStats(principal.getId());
        return ResponseEntity.ok(com.chitieu.web.dto.UserStatsResponse.builder()
                .transactionCount(stats.getTransactionCount())
                .friendCount(stats.getFriendCount())
                .goalCount(stats.getGoalCount())
                .build());
    }
}
