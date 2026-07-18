package com.enterprise.hrm.salary.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PayslipGenerateRequest {

    @NotNull @Positive
    private Long employeeId;

    @NotNull @Min(1) @Max(12)
    private Integer month;

    @NotNull @Min(2000)
    private Integer year;
}
