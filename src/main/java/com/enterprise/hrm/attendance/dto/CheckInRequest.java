package com.enterprise.hrm.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckInRequest {

    @NotNull(message = "Employee ID is required")
    @Positive
    private Long employeeId;

    /** Optional — defaults to today if not provided */
    private LocalDate attendanceDate;

    private String notes;
}
