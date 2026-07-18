package com.enterprise.hrm.leave.entity;

import com.enterprise.hrm.auth.entity.User;
import com.enterprise.hrm.common.BaseEntity;
import com.enterprise.hrm.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================
 * LEAVE REQUEST ENTITY
 * ============================================================
 *
 * Represents an employee's leave application.
 *
 * RELATIONSHIPS:
 *   LeaveRequest (Many) ──── (One) Employee     [ManyToOne] — who is applying
 *   LeaveRequest (Many) ──── (One) LeaveType    [ManyToOne] — type of leave
 *   LeaveRequest (Many) ──── (One) User         [ManyToOne] — who approved/rejected
 *
 * NOTE: We store approvedBy as a User reference (not Employee)
 * because HR managers have User accounts but may not be in the
 * Employee table (separation of concerns).
 *
 * @PrePersist:
 *   JPA lifecycle callback — runs BEFORE the entity is first persisted.
 *   We use it to calculate the number of days requested.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "leave_requests")
public class LeaveRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee applying for leave.
     * Many leave requests from one employee.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Type of leave being requested.
     * EAGER — always needed when displaying a leave request.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** Number of working days requested (startDate to endDate inclusive) */
    @Column(name = "number_of_days", nullable = false)
    private int numberOfDays;

    @Column(nullable = false, length = 1000)
    private String reason;

    /**
     * @Enumerated(STRING) — stored as text for readability
     * Default: PENDING on creation
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    /** User (HR/ADMIN) who approved or rejected this request */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    /** Timestamp of approval/rejection */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /** Optional note from approver (reason for rejection etc.) */
    @Column(name = "approver_note", length = 500)
    private String approverNote;
}
