package com.chitieu.domain.service;

import com.chitieu.domain.model.Debt;
import com.chitieu.domain.repository.DebtRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepositoryPort debtRepository;

    public List<Debt> getUserDebts(UUID userId) {
        return debtRepository.findByUserId(userId);
    }

    public Debt createDebt(Debt debt) {
        return debtRepository.save(debt);
    }

    public Debt markAsPaid(UUID debtId) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new RuntimeException("Debt not found"));

        Debt updated = Debt.builder()
                .id(debt.getId())
                .userId(debt.getUserId())
                .personName(debt.getPersonName())
                .amount(debt.getAmount())
                .type(debt.getType())
                .dueDate(debt.getDueDate())
                .interestRate(debt.getInterestRate())
                .status("PAID")
                .note(debt.getNote())
                .build();

        return debtRepository.save(updated);
    }
}
