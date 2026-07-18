package com.enterprise.hrm.leave.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Leave type response DTO.
 */
@Data
@Builder
public class LeaveTypeResponse {
    private Long id;
    private String name;
    private String description;
    private int maxDaysPerYear;
    private boolean paid;
    private boolean active;
}
