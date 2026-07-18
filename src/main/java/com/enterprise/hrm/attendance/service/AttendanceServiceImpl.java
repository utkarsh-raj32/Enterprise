package com.enterprise.hrm.attendance.service;

import com.enterprise.hrm.attendance.dto.AttendanceResponse;
import com.enterprise.hrm.attendance.dto.AttendanceSummaryResponse;
import com.enterprise.hrm.attendance.dto.CheckInRequest;
import com.enterprise.hrm.attendance.entity.Attendance;
import com.enterprise.hrm.attendance.entity.AttendanceStatus;
import com.enterprise.hrm.attendance.repository.AttendanceRepository;
import com.enterprise.hrm.common.PageResponse;
import com.enterprise.hrm.employee.entity.Employee;
import com.enterprise.hrm.employee.repository.EmployeeRepository;
import com.enterprise.hrm.exception.BusinessException;
import com.enterprise.hrm.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ============================================================
 * ATTENDANCE SERVICE IMPLEMENTATION
 * ============================================================
 *
 * Business Rules:
 * 1. Cannot check in twice on the same day
 * 2. Cannot check out without checking in first
 * 3. Cannot check out if already checked out
 * 4. Late threshold: after 09:30 AM = LATE status
 * 5. Work hours = checkout time - checkin time
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {

    private static final LocalTime WORK_START_TIME  = LocalTime.of(9, 0);
    private static final LocalTime LATE_THRESHOLD   = LocalTime.of(9, 30);
    private static final double HALF_DAY_HOURS      = 4.0;

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public AttendanceResponse checkIn(CheckInRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        LocalDate date = request.getAttendanceDate() != null
                ? request.getAttendanceDate()
                : LocalDate.now();

        // Business rule: Can't check in twice on same day
        attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), date)
                .ifPresent(a -> {
                    throw new BusinessException(
                            "Employee already checked in for today at " + a.getCheckIn(),
                            "ALREADY_CHECKED_IN"
                    );
                });

        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);

        // Determine status based on check-in time
        AttendanceStatus status = now.isAfter(LATE_THRESHOLD)
                ? AttendanceStatus.LATE
                : AttendanceStatus.PRESENT;

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .attendanceDate(date)
                .checkIn(now)
                .status(status)
                .notes(request.getNotes())
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Employee {} checked in at {}", employee.getEmpCode(), now);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(Long attendanceId) {
        Attendance attendance = findAttendanceOrThrow(attendanceId);

        if (attendance.getCheckIn() == null) {
            throw new BusinessException("Cannot check out without checking in first", "NOT_CHECKED_IN");
        }
        if (attendance.getCheckOut() != null) {
            throw new BusinessException("Already checked out at " + attendance.getCheckOut(), "ALREADY_CHECKED_OUT");
        }

        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        attendance.setCheckOut(now);

        // Calculate work hours
        long minutesWorked = ChronoUnit.MINUTES.between(attendance.getCheckIn(), now);
        double hoursWorked = minutesWorked / 60.0;
        attendance.setWorkHours(hoursWorked);

        // Update status based on hours worked
        if (hoursWorked < HALF_DAY_HOURS) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Employee checked out. Work hours: {}", hoursWorked);
        return mapToResponse(saved);
    }

    @Override
    public AttendanceResponse getTodayAttendance(Long employeeId) {
        return attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, LocalDate.now())
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    public AttendanceResponse getAttendanceById(Long id) {
        return mapToResponse(findAttendanceOrThrow(id));
    }

    @Override
    public PageResponse<AttendanceResponse> getEmployeeAttendance(Long employeeId, Pageable pageable) {
        Page<Attendance> page = attendanceRepository.findByEmployeeId(employeeId, pageable);
        List<AttendanceResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<AttendanceResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    @Override
    public List<AttendanceResponse> getMonthlyAttendance(Long employeeId, int year, int month) {
        return attendanceRepository.findMonthlyAttendance(employeeId, year, month)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceSummaryResponse getMonthlyAttendanceSummary(Long employeeId, int year, int month) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        List<Object[]> rawSummary = attendanceRepository.getAttendanceSummary(employeeId, year, month);
        Map<String, Long> statusCounts = new HashMap<>();
        for (Object[] row : rawSummary) {
            statusCounts.put(row[0].toString(), (Long) row[1]);
        }

        List<Attendance> monthlyRecords = attendanceRepository.findMonthlyAttendance(employeeId, year, month);
        double totalWorkHours = monthlyRecords.stream()
                .filter(a -> a.getWorkHours() != null)
                .mapToDouble(Attendance::getWorkHours)
                .sum();

        return AttendanceSummaryResponse.builder()
                .employeeId(employeeId)
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .year(year)
                .month(month)
                .totalWorkingDays(monthlyRecords.size())
                .presentDays(statusCounts.getOrDefault("PRESENT", 0L).intValue())
                .lateDays(statusCounts.getOrDefault("LATE", 0L).intValue())
                .halfDays(statusCounts.getOrDefault("HALF_DAY", 0L).intValue())
                .absentDays(statusCounts.getOrDefault("ABSENT", 0L).intValue())
                .leaveDays(statusCounts.getOrDefault("ON_LEAVE", 0L).intValue())
                .totalWorkHours(totalWorkHours)
                .statusCounts(statusCounts)
                .build();
    }

    private Attendance findAttendanceOrThrow(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));
    }

    private AttendanceResponse mapToResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .employeeId(a.getEmployee().getId())
                .employeeName(a.getEmployee().getFirstName() + " " + a.getEmployee().getLastName())
                .employeeCode(a.getEmployee().getEmpCode())
                .attendanceDate(a.getAttendanceDate())
                .checkIn(a.getCheckIn())
                .checkOut(a.getCheckOut())
                .workHours(a.getWorkHours())
                .status(a.getStatus())
                .notes(a.getNotes())
                .build();
    }
}
