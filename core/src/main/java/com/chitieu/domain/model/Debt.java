package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class Debt {
    private final UUID id;
    private final UUID userId;
    private final String personName;
    private final BigDecimal amount;
    private final DebtType type; // LEND (Cho vay) or BORROW (Đi vay)
    private final LocalDate dueDate;
    private final BigDecimal interestRate;
    private final String status; // ACTIVE, PAID
    private final String note;
}
