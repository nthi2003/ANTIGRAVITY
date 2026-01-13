package com.chitieu.web.controller;

import com.chitieu.domain.model.FriendProfile;
import com.chitieu.domain.model.Friendship;
import com.chitieu.domain.service.FriendshipService;
import com.chitieu.web.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {
    private final FriendshipService friendshipService;
    private final SecurityUtils securityUtils;

    @PostMapping("/request/{friendId}")
    public ResponseEntity<Friendship> sendFriendRequest(@PathVariable UUID friendId) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(friendshipService.sendFriendRequest(userId, friendId));
    }

    @PutMapping("/request/{friendId}/accept")
    public ResponseEntity<Friendship> acceptFriendRequest(@PathVariable UUID friendId) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(userId, friendId));
    }

    @DeleteMapping("/request/{friendId}/reject")
    public ResponseEntity<Void> rejectFriendRequest(@PathVariable UUID friendId) {
        UUID userId = securityUtils.getCurrentUserId();
        friendshipService.rejectFriendRequest(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable UUID friendId) {
        UUID userId = securityUtils.getCurrentUserId();
        friendshipService.removeFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<FriendProfile>> getFriends() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(friendshipService.getFriends(userId));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendProfile>> getPendingRequests() {
        log.info("REST request to get pending friend requests");
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(friendshipService.getPendingRequests(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FriendProfile>> searchFriends(@RequestParam String query) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(friendshipService.searchFriends(userId, query));
    }

    @GetMapping("/discover")
    public ResponseEntity<List<FriendProfile>> discoverUsers() {
        log.info("REST request to discover users");
        UUID userId = securityUtils.getCurrentUserId();
        try {
            List<FriendProfile> result = friendshipService.discoverUsers(userId);
            log.info("Discovery found {} users", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in discoverUsers controller: ", e);
            throw e;
        }
    }
}
