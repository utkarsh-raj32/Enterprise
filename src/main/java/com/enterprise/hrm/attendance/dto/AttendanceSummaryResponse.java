package com.enterprise.hrm.attendance.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Monthly attendance summary for an employee.
 * Contains counts of each attendance status for the month.
 */
@Data
@Builder
public class AttendanceSummaryResponse {
    private Long employeeId;
    private String employeeName;
    private int year;
    private int month;
    private int totalWorkingDays;
    private int presentDays;
    private int lateDays;
    private int halfDays;
    private int absentDays;
    private int leaveDays;
    private double totalWorkHours;
    private Map<String, Long> statusCounts; // Raw counts by status
}
