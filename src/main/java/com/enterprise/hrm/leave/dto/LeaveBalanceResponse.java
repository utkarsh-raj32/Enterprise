package com.enterprise.hrm.leave.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Leave balance response DTO for an employee.
 */
@Data
@Builder
public class LeaveBalanceResponse {
    private Long id;
    private String leaveTypeName;
    private boolean paid;
    private int year;
    private int totalDays;
    private int usedDays;
    private int remainingDays;
}
