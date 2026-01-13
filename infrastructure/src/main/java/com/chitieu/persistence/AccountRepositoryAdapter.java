package com.chitieu.persistence;

import com.chitieu.domain.model.Account;
import com.chitieu.domain.repository.AccountRepositoryPort;
import com.chitieu.persistence.entity.AccountEntity;
import com.chitieu.persistence.entity.UserEntity;
import com.chitieu.persistence.repository.AccountRepository;
import com.chitieu.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    public List<Account> findByUserId(UUID userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return accountRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Account save(Account account) {
        UserEntity user = userRepository.findById(account.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AccountEntity entity = AccountEntity.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .balance(account.getBalance())
                .currency(account.getCurrency() != null ? account.getCurrency() : "VND")
                .creditLimit(account.getCreditLimit())
                .user(user)
                .build();

        AccountEntity saved = accountRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        accountRepository.deleteById(id);
    }

    @Override
    public java.util.Map<UUID, java.math.BigDecimal> findTotalBalanceByUserIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        List<Object[]> results = accountRepository.findTotalBalanceByUserIds(userIds);

        return results.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (java.math.BigDecimal) row[1]));
    }

    private Account mapToDomain(AccountEntity entity) {
        return Account.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .balance(entity.getBalance())
                .currency(entity.getCurrency())
                .creditLimit(entity.getCreditLimit())
                .userId(entity.getUser().getId())
                .build();
    }
}
