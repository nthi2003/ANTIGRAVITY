package com.chitieu.persistence;

import com.chitieu.domain.model.WithdrawalRequest;
import com.chitieu.domain.repository.WithdrawalRequestRepositoryPort;
import com.chitieu.persistence.entity.GoalEntity;
import com.chitieu.persistence.entity.UserEntity;
import com.chitieu.persistence.entity.WithdrawalApprovalEntity;
import com.chitieu.persistence.entity.WithdrawalRequestEntity;
import com.chitieu.persistence.repository.GoalRepository;
import com.chitieu.persistence.repository.UserRepository;
import com.chitieu.persistence.repository.WithdrawalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WithdrawalRequestRepositoryAdapter implements WithdrawalRequestRepositoryPort {

        private final WithdrawalRequestRepository repository;
        private final GoalRepository goalRepository;
        private final UserRepository userRepository;

        @Override
        public WithdrawalRequest save(WithdrawalRequest request) {
                GoalEntity goal = goalRepository.findById(request.getGoalId())
                                .orElseThrow(() -> new RuntimeException("Goal not found"));
                UserEntity requester = userRepository.findById(request.getRequesterId())
                                .orElseThrow(() -> new RuntimeException("Requester not found"));

                WithdrawalRequestEntity entity = repository.findById(request.getId())
                                .orElse(WithdrawalRequestEntity.builder().id(request.getId()).build());

                entity.setGoal(goal);
                entity.setRequester(requester);
                entity.setAmount(request.getAmount());
                entity.setDescription(request.getDescription());
                entity.setStatus(request.getStatus());
                entity.setCreatedAt(request.getCreatedAt());

                if (request.getApprovals() != null) {
                        List<WithdrawalApprovalEntity> approvalEntities = request.getApprovals().stream()
                                        .map(a -> {
                                                UserEntity approver = userRepository.findById(a.getUserId())
                                                                .orElseThrow(() -> new RuntimeException(
                                                                                "Approver not found"));
                                                return WithdrawalApprovalEntity.builder()
                                                                .request(entity)
                                                                .approver(approver)
                                                                .status(a.getStatus())
                                                                .updatedAt(a.getUpdatedAt())
                                                                .build();
                                        })
                                        .collect(Collectors.toList());
                        if (entity.getApprovals() != null) {
                                entity.getApprovals().clear();
                                entity.getApprovals().addAll(approvalEntities);
                        } else {
                                entity.setApprovals(approvalEntities);
                        }
                }

                WithdrawalRequestEntity saved = repository.save(entity);
                return mapToDomain(saved);
        }

        @Override
        public Optional<WithdrawalRequest> findById(UUID id) {
                return repository.findById(id).map(this::mapToDomain);
        }

        @Override
        public List<WithdrawalRequest> findByGoalId(UUID goalId) {
                return repository.findByGoalId(goalId).stream()
                                .map(this::mapToDomain)
                                .collect(Collectors.toList());
        }

        private WithdrawalRequest mapToDomain(WithdrawalRequestEntity entity) {
                return WithdrawalRequest.builder()
                                .id(entity.getId())
                                .goalId(entity.getGoal().getId())
                                .requesterId(entity.getRequester().getId())
                                .requesterName(entity.getRequester().getFullName())
                                .amount(entity.getAmount())
                                .description(entity.getDescription())
                                .status(entity.getStatus())
                                .createdAt(entity.getCreatedAt())
                                .approvals(entity.getApprovals() != null ? entity.getApprovals().stream()
                                                .map(a -> WithdrawalRequest.Approval.builder()
                                                                .userId(a.getApprover().getId())
                                                                .userName(a.getApprover().getFullName())
                                                                .status(a.getStatus())
                                                                .updatedAt(a.getUpdatedAt())
                                                                .build())
                                                .collect(Collectors.toList()) : java.util.Collections.emptyList())
                                .build();
        }
}
