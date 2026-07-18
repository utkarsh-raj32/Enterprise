package com.enterprise.hrm.leave.repository;

import com.enterprise.hrm.leave.entity.LeaveRequest;
import com.enterprise.hrm.leave.entity.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);

    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    /**
     * Check for overlapping leave dates for an employee.
     * Prevents duplicate/overlapping leave applications.
     * Two date ranges overlap if: startA <= endB AND startB <= endA
     */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.employee.id = :employeeId
            AND lr.status IN ('PENDING', 'APPROVED')
            AND lr.startDate <= :endDate
            AND lr.endDate >= :startDate
            """)
    List<LeaveRequest> findOverlappingLeaves(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Count leaves by employee and status for a given year.
     */
    @Query("""
            SELECT COUNT(lr) FROM LeaveRequest lr
            WHERE lr.employee.id = :employeeId
            AND lr.status = :status
            AND YEAR(lr.startDate) = :year
            """)
    long countByEmployeeIdAndStatusAndYear(
            @Param("employeeId") Long employeeId,
            @Param("status") LeaveStatus status,
            @Param("year") int year);
}
