package com.chitieu.domain.service;

import com.chitieu.domain.model.Goal;
import com.chitieu.domain.model.Settlement;
import com.chitieu.domain.repository.GoalRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class GoalServiceImpl implements GoalService {

        private final GoalRepositoryPort goalRepository;
        private final com.chitieu.domain.repository.WithdrawalRequestRepositoryPort withdrawalRequestRepository;
        private final NotificationService notificationService;

        @Override
        public List<Goal> getUserGoals(UUID userId) {
                return goalRepository.findByUserId(userId);
        }

        @Override
        public void contribute(UUID goalId, UUID userId, BigDecimal amount) {
                goalRepository.updateContribution(goalId, userId, amount);
        }

        @Override
        public void requestWithdrawal(UUID goalId, UUID requesterId, BigDecimal amount, String description) {
                Goal goal = goalRepository.findById(goalId)
                                .orElseThrow(() -> new RuntimeException("Goal not found"));

                if (goal.getCurrentAmount().compareTo(amount) < 0) {
                        throw new RuntimeException("Số tiền yêu cầu rút (" + amount
                                        + ") lớn hơn số dư hiện tại của quỹ (" + goal.getCurrentAmount() + ")");
                }

                if (!goal.isLocked()) {
                        // If goal is not locked, maybe simple withdrawal?
                        // But according to user, "common fund" should be locked/monitored.
                }

                com.chitieu.domain.model.WithdrawalRequest request = com.chitieu.domain.model.WithdrawalRequest
                                .builder()
                                .id(UUID.randomUUID())
                                .goalId(goalId)
                                .requesterId(requesterId)
                                .amount(amount)
                                .description(description)
                                .status(com.chitieu.domain.model.ApprovalStatus.PENDING)
                                .createdAt(java.time.LocalDateTime.now())
                                .approvals(goal.getMembers().stream()
                                                .map(m -> com.chitieu.domain.model.WithdrawalRequest.Approval.builder()
                                                                .userId(m.getUserId())
                                                                .userName(m.getUserName())
                                                                .status(m.getUserId().equals(requesterId)
                                                                                ? com.chitieu.domain.model.ApprovalStatus.APPROVED
                                                                                : com.chitieu.domain.model.ApprovalStatus.PENDING)
                                                                .updatedAt(java.time.LocalDateTime.now())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build();

                withdrawalRequestRepository.save(request);

                // Notify only other members (not the requester)
                String requesterName = goal.getMembers().stream()
                                .filter(m -> m.getUserId().equals(requesterId))
                                .map(Goal.GoalMember::getUserName)
                                .findFirst()
                                .orElse("Thành viên");

                java.util.Map<String, String> metadata = new java.util.HashMap<>();
                metadata.put("goalId", goalId.toString());

                goal.getMembers().stream()
                                .filter(m -> !m.getUserId().equals(requesterId)) // Only notify other members
                                .forEach(m -> {
                                        notificationService.sendNotification(
                                                        m.getUserId(),
                                                        "Yêu cầu rút tiền mới",
                                                        String.format("Thành viên %s muốn rút %s từ quỹ %s",
                                                                        requesterName,
                                                                        amount.toString(),
                                                                        goal.getTitle()),
                                                        "WITHDRAWAL_REQUEST",
                                                        metadata);
                                });

        }

        @Override
        public void approveWithdrawal(UUID requestId, UUID userId, com.chitieu.domain.model.ApprovalStatus status) {
                com.chitieu.domain.model.WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                                .orElseThrow(() -> new RuntimeException("Request not found"));

                List<com.chitieu.domain.model.WithdrawalRequest.Approval> updatedApprovals = request.getApprovals()
                                .stream()
                                .map(a -> a.getUserId().equals(
                                                userId) ? com.chitieu.domain.model.WithdrawalRequest.Approval.builder()
                                                                .userId(a.getUserId())
                                                                .userName(a.getUserName())
                                                                .status(status)
                                                                .updatedAt(java.time.LocalDateTime.now())
                                                                .build() : a)
                                .collect(Collectors.toList());

                boolean allApproved = updatedApprovals.stream()
                                .allMatch(a -> a.getStatus() == com.chitieu.domain.model.ApprovalStatus.APPROVED);
                boolean anyRejected = updatedApprovals.stream()
                                .anyMatch(a -> a.getStatus() == com.chitieu.domain.model.ApprovalStatus.REJECTED);

                com.chitieu.domain.model.ApprovalStatus finalStatus = request.getStatus();
                if (allApproved)
                        finalStatus = com.chitieu.domain.model.ApprovalStatus.APPROVED;
                else if (anyRejected)
                        finalStatus = com.chitieu.domain.model.ApprovalStatus.REJECTED;

                com.chitieu.domain.model.WithdrawalRequest updatedRequest = com.chitieu.domain.model.WithdrawalRequest
                                .builder()
                                .id(request.getId())
                                .goalId(request.getGoalId())
                                .requesterId(request.getRequesterId())
                                .amount(request.getAmount())
                                .description(request.getDescription())
                                .status(finalStatus)
                                .createdAt(request.getCreatedAt())
                                .approvals(updatedApprovals)
                                .build();

                withdrawalRequestRepository.save(updatedRequest);

                if (finalStatus == com.chitieu.domain.model.ApprovalStatus.APPROVED) {
                        goalRepository.deductAmount(updatedRequest.getGoalId(), updatedRequest.getAmount());
                }

                Goal goal = goalRepository.findById(updatedRequest.getGoalId())
                                .orElseThrow(() -> new RuntimeException("Goal not found"));

                // Notify requester of status update
                notificationService.sendNotification(
                                updatedRequest.getRequesterId(),
                                "Cập nhật yêu cầu rút tiền",
                                String.format("Yêu cầu rút %s từ quỹ %s của bạn đã được cập nhật trạng thái: %s",
                                                updatedRequest.getAmount().toString(),
                                                goal.getTitle(),
                                                finalStatus == com.chitieu.domain.model.ApprovalStatus.APPROVED
                                                                ? "ĐÃ DUYỆT"
                                                                : finalStatus == com.chitieu.domain.model.ApprovalStatus.REJECTED
                                                                                ? "BỊ TỪ CHỐI"
                                                                                : "ĐANG CHỜ"),
                                "WITHDRAWAL_STATUS_UPDATE");
        }

        @Override
        public List<com.chitieu.domain.model.WithdrawalRequest> getWithdrawalRequests(UUID goalId) {
                return withdrawalRequestRepository.findByGoalId(goalId);
        }

        @Override
        public List<Settlement> calculateSettlements(UUID goalId) {
                Goal goal = goalRepository.findById(goalId)
                                .orElseThrow(() -> new RuntimeException("Goal not found"));

                List<MemberBalance> balances = goal.getMembers().stream()
                                .map(m -> new MemberBalance(
                                                m.getUserId(),
                                                m.getUserName(),
                                                m.getContributedAmount().subtract(m.getTargetAmount())))
                                .collect(Collectors.toList());

                List<Settlement> settlements = new ArrayList<>();

                // Simple Greedy Settlement Algorithm
                List<MemberBalance> debtors = balances.stream()
                                .filter(b -> b.balance.compareTo(BigDecimal.ZERO) < 0)
                                .sorted(Comparator.comparing(b -> b.balance))
                                .collect(Collectors.toList());

                List<MemberBalance> creditors = balances.stream()
                                .filter(b -> b.balance.compareTo(BigDecimal.ZERO) > 0)
                                .sorted(Comparator.comparing((MemberBalance b) -> b.balance).reversed())
                                .collect(Collectors.toList());

                int d = 0, c = 0;
                while (d < debtors.size() && c < creditors.size()) {
                        MemberBalance debtor = debtors.get(d);
                        MemberBalance creditor = creditors.get(c);

                        BigDecimal amountToPay = debtor.balance.abs().min(creditor.balance);

                        settlements.add(Settlement.builder()
                                        .fromUserId(debtor.userId)
                                        .fromUserName(debtor.userName)
                                        .toUserId(creditor.userId)
                                        .toUserName(creditor.userName)
                                        .amount(amountToPay)
                                        .build());

                        debtor.balance = debtor.balance.add(amountToPay);
                        creditor.balance = creditor.balance.subtract(amountToPay);

                        if (debtor.balance.compareTo(BigDecimal.ZERO) == 0)
                                d++;
                        if (creditor.balance.compareTo(BigDecimal.ZERO) == 0)
                                c++;
                }

                return settlements;
        }

        private static class MemberBalance {
                UUID userId;
                String userName;
                BigDecimal balance;

                MemberBalance(UUID userId, String userName, BigDecimal balance) {
                        this.userId = userId;
                        this.userName = userName;
                        this.balance = balance;
                }
        }
}
