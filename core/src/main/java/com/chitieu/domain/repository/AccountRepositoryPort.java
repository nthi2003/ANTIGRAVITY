package com.chitieu.domain.repository;

import com.chitieu.domain.model.Account;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {
    List<Account> findByUserId(UUID userId);

    Optional<Account> findById(UUID id);

    Account save(Account account);

    void deleteById(UUID id);

    java.util.Map<UUID, java.math.BigDecimal> findTotalBalanceByUserIds(List<UUID> userIds);
}
