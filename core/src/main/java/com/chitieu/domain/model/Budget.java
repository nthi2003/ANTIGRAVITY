package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class Budget {
    private final UUID id;
    private final String category;
    private final BigDecimal limitAmount;
    private final BigDecimal spentAmount;
    private final UUID userId;
}
