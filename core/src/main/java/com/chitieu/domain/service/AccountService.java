package com.chitieu.domain.service;

import com.chitieu.domain.model.Account;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    List<Account> getUserAccounts(UUID userId);

    Account createAccount(Account account);

    Account getAccountById(UUID id);

    Account updateAccount(UUID id, Account account);

    void updateBalance(UUID id, java.math.BigDecimal amount, String type);

    void deleteAccount(UUID id);
}
