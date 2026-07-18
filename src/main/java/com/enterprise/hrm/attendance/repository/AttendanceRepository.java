package com.enterprise.hrm.attendance.repository;

import com.enterprise.hrm.attendance.entity.Attendance;
import com.enterprise.hrm.attendance.entity.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);

    /**
     * Get monthly attendance for an employee.
     * YEAR() and MONTH() are JPQL functions.
     */
    @Query("""
            SELECT a FROM Attendance a
            WHERE a.employee.id = :employeeId
            AND YEAR(a.attendanceDate) = :year
            AND MONTH(a.attendanceDate) = :month
            ORDER BY a.attendanceDate ASC
            """)
    List<Attendance> findMonthlyAttendance(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month);

    /**
     * Summary counts by status for a given month.
     */
    @Query("""
            SELECT a.status, COUNT(a) FROM Attendance a
            WHERE a.employee.id = :employeeId
            AND YEAR(a.attendanceDate) = :year
            AND MONTH(a.attendanceDate) = :month
            GROUP BY a.status
            """)
    List<Object[]> getAttendanceSummary(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month);

    /** Find today's attendance for an employee */
    default Optional<Attendance> findTodayAttendance(Long employeeId) {
        return findByEmployeeIdAndAttendanceDate(employeeId, LocalDate.now());
    }
}
