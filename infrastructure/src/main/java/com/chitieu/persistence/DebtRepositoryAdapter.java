package com.chitieu.persistence;

import com.chitieu.domain.model.Debt;
import com.chitieu.domain.repository.DebtRepositoryPort;
import com.chitieu.persistence.entity.DebtEntity;
import com.chitieu.persistence.repository.DebtRepository;
import com.chitieu.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DebtRepositoryAdapter implements DebtRepositoryPort {

    private final DebtRepository debtRepository;
    private final UserRepository userRepository;

    @Override
    public List<Debt> findByUserId(UUID userId) {
        return debtRepository.findByUserId(userId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Debt save(Debt debt) {
        DebtEntity entity = mapToEntity(debt);
        return mapToDomain(debtRepository.save(entity));
    }

    @Override
    public Optional<Debt> findById(UUID id) {
        return debtRepository.findById(id).map(this::mapToDomain);
    }

    private Debt mapToDomain(DebtEntity entity) {
        return Debt.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .personName(entity.getPersonName())
                .amount(entity.getAmount())
                .type(entity.getType())
                .dueDate(entity.getDueDate())
                .interestRate(entity.getInterestRate())
                .status(entity.getStatus())
                .note(entity.getNote())
                .build();
    }

    private DebtEntity mapToEntity(Debt debt) {
        return DebtEntity.builder()
                .id(debt.getId())
                .user(userRepository.findById(debt.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found")))
                .personName(debt.getPersonName())
                .amount(debt.getAmount())
                .type(debt.getType())
                .dueDate(debt.getDueDate())
                .interestRate(debt.getInterestRate())
                .status(debt.getStatus())
                .note(debt.getNote())
                .build();
    }
}
