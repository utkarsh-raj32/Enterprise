package com.enterprise.hrm.salary.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class PayslipResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String departmentName;
    private String designation;
    private int month;
    private int year;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal allowances;
    private BigDecimal deductions;
    private BigDecimal netSalary;
    private int workingDays;
    private int presentDays;
    private LocalDateTime generatedAt;
    private String pdfUrl;
}
