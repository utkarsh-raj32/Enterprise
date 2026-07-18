package com.enterprise.hrm.attendance.service;

import com.enterprise.hrm.attendance.dto.AttendanceResponse;
import com.enterprise.hrm.attendance.dto.AttendanceSummaryResponse;
import com.enterprise.hrm.attendance.dto.CheckInRequest;
import com.enterprise.hrm.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AttendanceService {
    AttendanceResponse checkIn(CheckInRequest request);
    AttendanceResponse checkOut(Long attendanceId);
    AttendanceResponse getTodayAttendance(Long employeeId);
    AttendanceResponse getAttendanceById(Long id);
    PageResponse<AttendanceResponse> getEmployeeAttendance(Long employeeId, Pageable pageable);
    List<AttendanceResponse> getMonthlyAttendance(Long employeeId, int year, int month);
    AttendanceSummaryResponse getMonthlyAttendanceSummary(Long employeeId, int year, int month);
}
