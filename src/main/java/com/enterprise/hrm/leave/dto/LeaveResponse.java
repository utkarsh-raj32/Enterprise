package com.enterprise.hrm.leave.dto;

import com.enterprise.hrm.leave.entity.LeaveStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Leave request response DTO.
 */
@Data
@Builder
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfDays;
    private String reason;
    private LeaveStatus status;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String approverNote;
    private LocalDateTime createdAt;
}
