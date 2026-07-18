package com.enterprise.hrm.employee.entity;

import com.enterprise.hrm.auth.entity.User;
import com.enterprise.hrm.common.BaseEntity;
import com.enterprise.hrm.department.entity.Department;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ============================================================
 * EMPLOYEE ENTITY
 * ============================================================
 *
 * RELATIONSHIPS:
 *   Employee (Many) ──── (One) Department    [ManyToOne]
 *   Employee (One)  ──── (One) User          [OneToOne]
 *
 * @ManyToOne (Employee → Department):
 *   Many employees belong to one department.
 *   The FK 'department_id' lives in the 'employees' table.
 *   This side OWNS the relationship (has the FK column).
 *
 * @JoinColumn(name = "department_id"):
 *   Explicitly names the foreign key column.
 *   nullable = false — every employee must belong to a department.
 *
 * @OneToOne (Employee → User):
 *   Each employee has exactly one user account for login.
 *   FK 'user_id' in employees table points to users.id.
 *   cascade = ALL — creating/updating employee does the same to user.
 *
 * FetchType.LAZY vs EAGER:
 *   • @ManyToOne default: EAGER (loads dept with every employee)
 *   • We override to LAZY for department — fetch when needed
 *   • WHY? Loading ALL fields on a list query (100 employees) would
 *     trigger 100 separate dept queries (N+1 problem).
 *     Solution: use JOIN FETCH in JPQL when dept is needed.
 *
 * N+1 PROBLEM (Interview topic):
 *   If you load 100 employees with LAZY department, and call
 *   employee.getDepartment() for each → 100 extra SQL queries!
 *   Solutions:
 *   1. JOIN FETCH in JPQL: "SELECT e FROM Employee e JOIN FETCH e.department"
 *   2. @EntityGraph annotation
 *   3. @BatchSize(size = 25) — loads 25 departments per query
 *
 * @Enumerated(EnumType.STRING): stores "ACTIVE", "INACTIVE", "TERMINATED"
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employees",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "emp_code"),
        @UniqueConstraint(columnNames = "email")
    }
)
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable employee identifier.
     * Format: EMP-001, EMP-002, etc.
     */
    @Column(name = "emp_code", nullable = false, unique = true, length = 20)
    private String empCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(length = 150)
    private String designation;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    /**
     * Employment status as enum.
     * ACTIVE — currently employed
     * INACTIVE — on leave of absence / temporarily inactive
     * TERMINATED — no longer employed
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(precision = 15, scale = 2)
    private BigDecimal salary;

    @Column(length = 255)
    private String address;

    /**
     * @ManyToOne — Many employees in one department
     * LAZY — don't load department until explicitly accessed
     * @JoinColumn — FK column 'department_id' in employees table
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /**
     * @OneToOne — One employee has one user account
     * LAZY — only load user account when needed
     * cascade = ALL — employee lifecycle cascades to user
     * @JoinColumn — FK 'user_id' in employees table
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
}
