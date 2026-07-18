package com.enterprise.hrm.employee.dto;

import com.enterprise.hrm.employee.entity.EmployeeStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Employee creation/update request DTO.
 * Includes full Bean Validation constraints.
 */
@Data
public class EmployeeRequest {

    @NotBlank(message = "Employee code is required")
    @Pattern(regexp = "^EMP-\\d{3,6}$", message = "Employee code must match pattern EMP-001")
    private String empCode;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phone;

    @NotBlank(message = "Designation is required")
    @Size(max = 150)
    private String designation;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @NotNull(message = "Department ID is required")
    @Positive(message = "Department ID must be positive")
    private Long departmentId;

    @DecimalMin(value = "0.0", message = "Salary cannot be negative")
    private BigDecimal salary;

    @Size(max = 255)
    private String address;

    private EmployeeStatus status;
}
