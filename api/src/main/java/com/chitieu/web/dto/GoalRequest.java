package com.chitieu.web.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GoalRequest {
    private String title;
    private BigDecimal targetAmount;
}
