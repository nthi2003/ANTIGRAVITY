package com.chitieu.persistence;

import com.chitieu.domain.model.Transaction;
import com.chitieu.domain.repository.TransactionRepositoryPort;
import com.chitieu.persistence.entity.AccountEntity;
import com.chitieu.persistence.entity.TransactionEntity;
import com.chitieu.persistence.entity.UserEntity;
import com.chitieu.persistence.repository.AccountRepository;
import com.chitieu.persistence.repository.TransactionRepository;
import com.chitieu.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

        private final TransactionRepository transactionRepository;
        private final AccountRepository accountRepository;
        private final UserRepository userRepository;

        @Override
        public List<Transaction> findByUserId(UUID userId) {
                return transactionRepository.findByUserId(userId).stream()
                                .map(entity -> Transaction.builder()
                                                .id(entity.getId())
                                                .amount(entity.getAmount())
                                                .category(entity.getCategory())
                                                .type(entity.getType())
                                                .date(entity.getTransactionDate().toLocalDate())
                                                .accountId(entity.getAccount().getId())
                                                .build())
                                .collect(Collectors.toList());
        }

        @Override
        public void save(UUID userId, Transaction transaction) {
                UserEntity user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                AccountEntity account = accountRepository.findById(transaction.getAccountId())
                                .orElseThrow(() -> new RuntimeException("Account not found"));

                TransactionEntity entity = TransactionEntity.builder()
                                .id(transaction.getId())
                                .user(user)
                                .account(account)
                                .amount(transaction.getAmount())
                                .category(transaction.getCategory())
                                .type(transaction.getType())
                                .transactionDate(transaction.getDate().atStartOfDay())
                                .build();

                transactionRepository.save(entity);
        }

        @Override
        public long countByUserId(UUID userId) {
                return transactionRepository.countByUserId(userId);
        }
}
