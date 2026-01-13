package com.chitieu.web.controller;

import com.chitieu.domain.model.Goal;
import com.chitieu.domain.service.GoalService;
import com.chitieu.persistence.entity.UserEntity;
import com.chitieu.persistence.repository.UserRepository;
import com.chitieu.web.dto.GoalRequest;
import com.chitieu.web.dto.MemberRequest;
import com.chitieu.web.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final SecurityUtils securityUtils;
    private final com.chitieu.domain.repository.GoalRepositoryPort goalRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Goal>> getUserGoals() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(goalService.getUserGoals(userId));
    }

    @PostMapping
    public ResponseEntity<Goal> createGoal(@RequestBody GoalRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        Goal newGoal = Goal.builder()
                .title(request.getTitle())
                .targetAmount(request.getTargetAmount())
                .isLocked(false)
                .build();
        return ResponseEntity.ok(goalRepository.save(newGoal, userId));
    }

    @PostMapping("/{goalId}/members")
    public ResponseEntity<String> addMember(@PathVariable UUID goalId, @RequestBody MemberRequest request) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        com.chitieu.domain.model.GoalRole role = request.getRole() != null
                ? request.getRole()
                : com.chitieu.domain.model.GoalRole.MEMBER;

        goalRepository.addMember(goalId, user.getId(), request.getTargetAmount(), role);
        return ResponseEntity.ok("Member added successfully");
    }

    @GetMapping("/{goalId}/settlements")
    public ResponseEntity<List<com.chitieu.domain.model.Settlement>> getSettlements(@PathVariable UUID goalId) {
        return ResponseEntity.ok(goalService.calculateSettlements(goalId));
    }

    @PostMapping("/{goalId}/contribute")
    public ResponseEntity<String> contribute(@PathVariable UUID goalId, @RequestBody java.math.BigDecimal amount) {
        UUID userId = securityUtils.getCurrentUserId();
        goalService.contribute(goalId, userId, amount);
        return ResponseEntity.ok("Contribution recorded");
    }

    @PostMapping("/{goalId}/withdrawals")
    public ResponseEntity<String> requestWithdrawal(@PathVariable UUID goalId,
            @RequestBody com.chitieu.web.dto.WithdrawalRequestDto request) {
        UUID userId = securityUtils.getCurrentUserId();
        goalService.requestWithdrawal(goalId, userId, request.getAmount(), request.getDescription());
        return ResponseEntity.ok("Withdrawal request created");
    }

    @GetMapping("/{goalId}/withdrawals")
    public ResponseEntity<List<com.chitieu.domain.model.WithdrawalRequest>> getWithdrawals(@PathVariable UUID goalId) {
        return ResponseEntity.ok(goalService.getWithdrawalRequests(goalId));
    }

    @PostMapping("/withdrawals/{requestId}/approve")
    public ResponseEntity<String> approveWithdrawal(@PathVariable UUID requestId,
            @RequestBody com.chitieu.web.dto.ApprovalRequestDto request) {
        UUID userId = securityUtils.getCurrentUserId();
        goalService.approveWithdrawal(requestId, userId, request.getStatus());
        return ResponseEntity.ok("Status updated");
    }
}
