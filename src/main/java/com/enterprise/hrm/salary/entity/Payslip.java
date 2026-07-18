package com.enterprise.hrm.salary.entity;

import com.enterprise.hrm.common.BaseEntity;
import com.enterprise.hrm.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ============================================================
 * PAYSLIP ENTITY
 * ============================================================
 *
 * A generated payslip for an employee for a specific month/year.
 * References the SalaryStructure that was active at time of generation.
 *
 * UNIQUE CONSTRAINT: One payslip per (employee, month, year)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payslips",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_employee_payslip_month_year",
        columnNames = {"employee_id", "month", "year"}
    )
)
public class Payslip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_structure_id", nullable = false)
    private SalaryStructure salaryStructure;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    @Column(name = "basic_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal basicSalary;

    @Column(precision = 15, scale = 2)
    private BigDecimal hra;

    @Column(precision = 15, scale = 2)
    private BigDecimal allowances;

    @Column(precision = 15, scale = 2)
    private BigDecimal deductions;

    @Column(name = "net_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal netSalary;

    /** Working days in the month */
    @Column(name = "working_days")
    private int workingDays;

    /** Days the employee was present */
    @Column(name = "present_days")
    private int presentDays;

    @Column(name = "generated_at", nullable = false)
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();

    /** URL/path to the generated PDF payslip */
    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;
}
