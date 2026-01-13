package com.chitieu.web.controller;

import com.chitieu.domain.model.Debt;
import com.chitieu.domain.service.DebtService;
import com.chitieu.web.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService debtService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<Debt>> getMyDebts() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(debtService.getUserDebts(userId));
    }

    @PostMapping
    public ResponseEntity<Debt> createDebt(@RequestBody Debt debtRequest) {
        UUID userId = securityUtils.getCurrentUserId();
        Debt debt = Debt.builder()
                .userId(userId)
                .personName(debtRequest.getPersonName())
                .amount(debtRequest.getAmount())
                .type(debtRequest.getType())
                .dueDate(debtRequest.getDueDate())
                .interestRate(debtRequest.getInterestRate())
                .status("ACTIVE")
                .note(debtRequest.getNote())
                .build();
        return ResponseEntity.ok(debtService.createDebt(debt));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<Debt> payDebt(@PathVariable UUID id) {
        return ResponseEntity.ok(debtService.markAsPaid(id));
    }
}
