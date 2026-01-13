package com.chitieu.domain.service;

import com.chitieu.domain.model.Transaction;
import java.util.UUID;

public interface TransactionService {
    void recordTransaction(UUID userId, Transaction transaction);

    java.util.List<Transaction> getUserTransactions(UUID userId);
}
