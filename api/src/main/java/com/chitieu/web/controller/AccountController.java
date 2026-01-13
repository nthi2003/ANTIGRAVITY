package com.chitieu.web.controller;

import com.chitieu.domain.model.Account;
import com.chitieu.domain.service.AccountService;
import com.chitieu.web.dto.AccountRequest;
import com.chitieu.web.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<Account>> getMyAccounts() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(accountService.getUserAccounts(userId));
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody AccountRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        Account account = Account.builder()
                .name(request.getName())
                .type(request.getType())
                .balance(request.getBalance())
                .currency(request.getCurrency())
                .creditLimit(request.getCreditLimit())
                .userId(userId)
                .build();
        return ResponseEntity.ok(accountService.createAccount(account));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable UUID id, @RequestBody AccountRequest request) {
        Account account = Account.builder()
                .name(request.getName())
                .type(request.getType())
                .balance(request.getBalance())
                .currency(request.getCurrency())
                .creditLimit(request.getCreditLimit())
                .build();
        return ResponseEntity.ok(accountService.updateAccount(id, account));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok().build();
    }
}
