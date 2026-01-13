package com.chitieu.domain.service;

import com.chitieu.domain.model.Account;
import com.chitieu.domain.repository.AccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepositoryPort accountRepository;

    @Override
    public List<Account> getUserAccounts(UUID userId) {
        return accountRepository.findByUserId(userId);
    }

    @Override
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Account getAccountById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Override
    public Account updateAccount(UUID id, Account account) {
        Account existing = getAccountById(id);
        Account updated = Account.builder()
                .id(existing.getId())
                .name(account.getName())
                .type(account.getType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .creditLimit(account.getCreditLimit())
                .userId(existing.getUserId())
                .build();
        return accountRepository.save(updated);
    }

    @Override
    public void updateBalance(UUID id, java.math.BigDecimal amount, String type) {
        Account account = getAccountById(id);
        java.math.BigDecimal newBalance;
        if ("INCOME".equals(type)) {
            newBalance = account.getBalance().add(amount);
        } else {
            newBalance = account.getBalance().subtract(amount);
        }

        Account updated = Account.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .balance(newBalance)
                .currency(account.getCurrency())
                .creditLimit(account.getCreditLimit())
                .userId(account.getUserId())
                .build();
        accountRepository.save(updated);
    }

    @Override
    public void deleteAccount(UUID id) {
        accountRepository.deleteById(id);
    }
}
