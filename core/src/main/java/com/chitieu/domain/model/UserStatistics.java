package com.chitieu.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserStatistics {
    private final long transactionCount;
    private final long friendCount;
    private final long goalCount;
}
