package com.chitieu.web.dto;

import com.chitieu.domain.model.GoalRole;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MemberRequest {
    private String username;
    private BigDecimal targetAmount;
    private GoalRole role;
}
