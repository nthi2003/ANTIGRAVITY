package com.chitieu.web.dto;

import com.chitieu.domain.model.ApprovalStatus;
import lombok.Data;

@Data
public class ApprovalRequestDto {
    private ApprovalStatus status;
}
