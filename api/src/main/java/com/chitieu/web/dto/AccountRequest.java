package com.chitieu.web.dto;

import com.chitieu.domain.model.AccountType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountRequest {
    private String name;
    private AccountType type;
    private BigDecimal balance;
    private String currency;
    private BigDecimal creditLimit;
}
