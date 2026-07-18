package com.enterprise.hrm.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Department creation/update request DTO.
 */
@Data
public class DepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String name;

    /**
     * Department code — uppercase alphanumeric, 2-10 chars.
     * @Pattern ensures format like "ENG", "HR", "FIN"
     */
    @NotBlank(message = "Department code is required")
    @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "Code must be 2-10 uppercase letters/digits")
    private String code;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private String managerName;
}
