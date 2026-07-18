package com.enterprise.hrm.salary.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalaryStructureRequest {

    @NotNull @Positive
    private Long employeeId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Basic salary must be greater than 0")
    private BigDecimal basicSalary;

    @DecimalMin(value = "0.0")
    private BigDecimal hra;

    @DecimalMin(value = "0.0")
    private BigDecimal allowances;

    @DecimalMin(value = "0.0")
    private BigDecimal deductions;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;
}
