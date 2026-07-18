package com.enterprise.hrm.leave.repository;

import com.enterprise.hrm.leave.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(
            Long employeeId, Long leaveTypeId, int year);

    List<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, int year);

    @Query("SELECT lb FROM LeaveBalance lb " +
           "JOIN FETCH lb.leaveType " +
           "WHERE lb.employee.id = :employeeId AND lb.year = :year")
    List<LeaveBalance> findBalancesWithLeaveType(
            @Param("employeeId") Long employeeId,
            @Param("year") int year);
}
