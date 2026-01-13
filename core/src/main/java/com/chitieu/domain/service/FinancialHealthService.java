package com.chitieu.domain.service;

import com.chitieu.domain.model.FinancialHealthScore;
import java.util.UUID;

public interface FinancialHealthService {
    FinancialHealthScore calculateHealthScore(UUID userId);
}
