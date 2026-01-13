package com.chitieu.web.controller;

import com.chitieu.domain.model.Transaction;
import com.chitieu.domain.service.TransactionService;
import com.chitieu.web.dto.TransactionRequest;
import com.chitieu.web.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<Object> recordTransaction(@RequestBody TransactionRequest request) {
        UUID userId = securityUtils.getCurrentUserId();

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .category(request.getCategory())
                .type(request.getType() != null ? request.getType() : "EXPENSE")
                .date(LocalDate.now())
                .accountId(request.getAccountId())
                .build();

        transactionService.recordTransaction(userId, transaction);

        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Transaction recorded successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<java.util.List<Transaction>> getUserTransactions() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(transactionService.getUserTransactions(userId));
    }
}
