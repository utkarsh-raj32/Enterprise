package com.enterprise.hrm.attendance.controller;

import com.enterprise.hrm.attendance.dto.AttendanceResponse;
import com.enterprise.hrm.attendance.dto.AttendanceSummaryResponse;
import com.enterprise.hrm.attendance.dto.CheckInRequest;
import com.enterprise.hrm.attendance.service.AttendanceService;
import com.enterprise.hrm.common.ApiResponse;
import com.enterprise.hrm.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Attendance controller — 7 endpoints.
 */
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Employee attendance check-in/check-out and reporting APIs")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "Employee check-in")
    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(
            @Valid @RequestBody CheckInRequest request) {
        AttendanceResponse response = attendanceService.checkIn(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Check-in recorded successfully", response, 201));
    }

    @Operation(summary = "Employee check-out")
    @PutMapping("/checkout/{attendanceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(@PathVariable Long attendanceId) {
        return ResponseEntity.ok(
                ApiResponse.success("Check-out recorded", attendanceService.checkOut(attendanceId), 200)
        );
    }

    @Operation(summary = "Get today's attendance for an employee")
    @GetMapping("/today/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getTodayAttendance(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                ApiResponse.success("Today's attendance fetched",
                        attendanceService.getTodayAttendance(employeeId), 200)
        );
    }

    @Operation(summary = "Get attendance by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Attendance fetched", attendanceService.getAttendanceById(id), 200)
        );
    }

    @Operation(summary = "Get attendance history for an employee (paginated)")
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceResponse>>> getEmployeeAttendance(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<AttendanceResponse> response = attendanceService.getEmployeeAttendance(
                employeeId,
                PageRequest.of(page, size, Sort.by("attendanceDate").descending())
        );
        return ResponseEntity.ok(ApiResponse.success("Attendance history fetched", response, 200));
    }

    @Operation(summary = "Get monthly attendance for an employee")
    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getMonthlyAttendance(
            @RequestParam Long employeeId,
            @RequestParam int year,
            @RequestParam int month) {

        List<AttendanceResponse> response =
                attendanceService.getMonthlyAttendance(employeeId, year, month);
        return ResponseEntity.ok(ApiResponse.success("Monthly attendance fetched", response, 200));
    }

    @Operation(summary = "Get monthly attendance summary for an employee")
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<AttendanceSummaryResponse>> getMonthlyAttendanceSummary(
            @RequestParam Long employeeId,
            @RequestParam int year,
            @RequestParam int month) {

        AttendanceSummaryResponse response =
                attendanceService.getMonthlyAttendanceSummary(employeeId, year, month);
        return ResponseEntity.ok(ApiResponse.success("Attendance summary fetched", response, 200));
    }
}
