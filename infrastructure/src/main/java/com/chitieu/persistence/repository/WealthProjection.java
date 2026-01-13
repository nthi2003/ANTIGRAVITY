package com.chitieu.persistence.repository;

import java.math.BigDecimal;
import java.util.UUID;

public interface WealthProjection {
    UUID getUserId();

    String getFullName();

    String getUsername();

    BigDecimal getTotalWealth();
}
