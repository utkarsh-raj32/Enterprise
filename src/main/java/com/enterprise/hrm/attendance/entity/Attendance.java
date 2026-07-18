package com.enterprise.hrm.attendance.entity;

import com.enterprise.hrm.common.BaseEntity;
import com.enterprise.hrm.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * ============================================================
 * ATTENDANCE ENTITY
 * ============================================================
 *
 * Tracks daily attendance for each employee.
 * One record per employee per date.
 *
 * UNIQUE CONSTRAINT: (employee_id, attendance_date)
 * One employee can only have one attendance record per day.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendances",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_employee_date",
        columnNames = {"employee_id", "attendance_date"}
    )
)
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in")
    private LocalTime checkIn;

    @Column(name = "check_out")
    private LocalTime checkOut;

    /**
     * Work hours = checkOut - checkIn (calculated on checkout).
     * Stored as decimal hours (e.g., 7.5 = 7 hours 30 minutes).
     */
    @Column(name = "work_hours")
    private Double workHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    @Column(length = 500)
    private String notes;
}
