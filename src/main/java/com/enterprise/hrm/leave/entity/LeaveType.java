package com.enterprise.hrm.leave.entity;

import com.enterprise.hrm.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * ============================================================
 * LEAVE TYPE ENTITY
 * ============================================================
 *
 * Examples: Annual Leave, Sick Leave, Maternity Leave, Unpaid Leave
 * Each type has a maximum number of days per calendar year.
 *
 * @Table(name = "leave_types")
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "leave_types")
public class LeaveType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /**
     * Maximum days an employee can take of this leave type per year.
     * E.g., Annual Leave = 21, Sick Leave = 14
     */
    @Column(name = "max_days_per_year", nullable = false)
    private int maxDaysPerYear;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Whether this leave type is paid or unpaid */
    @Column(nullable = false)
    @Builder.Default
    private boolean paid = true;
}
