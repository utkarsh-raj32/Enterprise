package com.enterprise.hrm.leave.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Leave application request DTO.
 */
@Data
public class LeaveApplyRequest {

    @NotNull(message = "Employee ID is required")
    @Positive
    private Long employeeId;

    @NotNull(message = "Leave type ID is required")
    @Positive
    private Long leaveTypeId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 1000, message = "Reason must be 10-1000 characters")
    private String reason;
}
