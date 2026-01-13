package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class Account {
    private final UUID id;
    private final String name;
    private final AccountType type;
    private final BigDecimal balance;
    private final String currency;
    private final BigDecimal creditLimit; // Mandatory for CREDIT type
    private final UUID userId;
}
