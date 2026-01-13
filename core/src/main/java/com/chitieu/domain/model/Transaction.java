package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class Transaction {
    private final UUID id;
    private final BigDecimal amount;
    private final String category;
    private final String type; // INCOME, EXPENSE
    private final LocalDate date;
    private final UUID accountId;
}
