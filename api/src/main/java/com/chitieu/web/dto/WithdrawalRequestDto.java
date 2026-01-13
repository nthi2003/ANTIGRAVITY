package com.chitieu.web.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WithdrawalRequestDto {
    private BigDecimal amount;
    private String description;
}
