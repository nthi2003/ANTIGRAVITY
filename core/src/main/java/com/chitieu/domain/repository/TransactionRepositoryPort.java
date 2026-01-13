package com.chitieu.domain.repository;

import com.chitieu.domain.model.Transaction;
import java.util.List;
import java.util.UUID;

public interface TransactionRepositoryPort {
    List<Transaction> findByUserId(UUID userId);

    void save(UUID userId, Transaction transaction);

    long countByUserId(UUID userId);
}
