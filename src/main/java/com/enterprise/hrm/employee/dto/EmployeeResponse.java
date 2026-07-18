package com.enterprise.hrm.employee.dto;

import com.enterprise.hrm.employee.entity.EmployeeStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee response DTO.
 * Contains a nested DepartmentSummary to avoid sending full dept data.
 * This is a common pattern — embedding summarized sub-resources.
 */
@Data
@Builder
public class EmployeeResponse {

    private Long id;
    private String empCode;
    private String firstName;
    private String lastName;
    private String fullName;        // Computed: firstName + " " + lastName
    private String email;
    private String phone;
    private String designation;
    private LocalDate joiningDate;
    private EmployeeStatus status;
    private BigDecimal salary;
    private String address;

    /** Nested department summary — avoids N+1 or overfetching */
    private DepartmentSummary department;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Nested DTO for department info embedded in employee response.
     * Avoids returning the full DepartmentResponse with all employees.
     * This is the "slim reference" pattern.
     */
    @Data
    @Builder
    public static class DepartmentSummary {
        private Long id;
        private String name;
        private String code;
    }
}
