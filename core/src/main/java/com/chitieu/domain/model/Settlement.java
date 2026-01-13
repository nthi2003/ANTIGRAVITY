package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class Settlement {
    private final UUID fromUserId;
    private final String fromUserName;
    private final UUID toUserId;
    private final String toUserName;
    private final BigDecimal amount;
}
