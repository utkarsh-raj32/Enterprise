package com.enterprise.hrm.leave.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request DTO for creating a new leave type.
 */
@Data
public class LeaveTypeRequest {

    @NotBlank(message = "Leave type name is required")
    @Size(min = 3, max = 100, message = "Name must be 3-100 characters")
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Max days per year is required")
    @Min(value = 1, message = "Must be at least 1 day")
    @Max(value = 365, message = "Cannot exceed 365 days")
    private Integer maxDaysPerYear;

    private boolean paid = true;
}
