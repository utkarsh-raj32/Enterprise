package com.enterprise.hrm.leave.entity;

import com.enterprise.hrm.common.BaseEntity;
import com.enterprise.hrm.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

/**
 * ============================================================
 * LEAVE BALANCE ENTITY
 * ============================================================
 *
 * Tracks each employee's leave balance per leave type per year.
 *
 * RELATIONSHIPS:
 *   LeaveBalance (Many) ──── (One) Employee    [ManyToOne]
 *   LeaveBalance (Many) ──── (One) LeaveType   [ManyToOne]
 *
 * Unique constraint: one balance per (employee, leave_type, year)
 * This prevents duplicate balance records.
 *
 * @Table uniqueConstraints:
 *   DB-level unique constraint across multiple columns.
 *   Application-level: service checks before creating duplicate.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "leave_balances",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_employee_leavetype_year",
        columnNames = {"employee_id", "leave_type_id", "year"}
    )
)
public class LeaveBalance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @ManyToOne — Many balances belong to one employee
     * LAZY — only load employee when explicitly accessed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * @ManyToOne — Many balances can be for the same leave type
     * EAGER — leave type is small and always needed with balance
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    /** Calendar year for this balance (e.g., 2024) */
    @Column(nullable = false)
    private int year;

    /** Total days allocated (usually = leaveType.maxDaysPerYear) */
    @Column(name = "total_days", nullable = false)
    private int totalDays;

    /** Days used so far this year */
    @Column(name = "used_days", nullable = false)
    @Builder.Default
    private int usedDays = 0;

    /** Computed: totalDays - usedDays */
    @Column(name = "remaining_days", nullable = false)
    private int remainingDays;

    /** Called before insert to compute remainingDays */
    @PrePersist
    @PreUpdate
    public void calculateRemainingDays() {
        this.remainingDays = this.totalDays - this.usedDays;
    }
}
