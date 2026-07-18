package com.enterprise.hrm.salary.entity;

import com.enterprise.hrm.common.BaseEntity;
import com.enterprise.hrm.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ============================================================
 * SALARY STRUCTURE ENTITY
 * ============================================================
 *
 * Defines the salary breakdown for an employee.
 * One employee can have multiple salary structures over time
 * (salary revisions). The latest one by effectiveDate is current.
 *
 * @ManyToOne (SalaryStructure → Employee):
 *   Many salary revisions for one employee over their tenure.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "salary_structures")
public class SalaryStructure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /** Base salary amount */
    @Column(name = "basic_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal basicSalary;

    /** House Rent Allowance (typically 40-50% of basic) */
    @Column(precision = 15, scale = 2)
    private BigDecimal hra;

    /** Other allowances (transport, food, medical etc.) */
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal allowances = BigDecimal.ZERO;

    /** Deductions (PF, tax, etc.) */
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal deductions = BigDecimal.ZERO;

    /**
     * Net salary = basicSalary + hra + allowances - deductions
     * Calculated and stored for quick payslip generation.
     */
    @Column(name = "net_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal netSalary;

    /** Date from which this salary structure is effective */
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Calculate net salary before persisting */
    @PrePersist
    @PreUpdate
    public void calculateNetSalary() {
        BigDecimal gross = basicSalary
                .add(hra != null ? hra : BigDecimal.ZERO)
                .add(allowances != null ? allowances : BigDecimal.ZERO);
        this.netSalary = gross.subtract(deductions != null ? deductions : BigDecimal.ZERO);
    }
}
