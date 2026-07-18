package com.enterprise.hrm.leave.controller;

import com.enterprise.hrm.common.ApiResponse;
import com.enterprise.hrm.common.PageResponse;
import com.enterprise.hrm.leave.dto.*;
import com.enterprise.hrm.leave.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;

/**
 * ============================================================
 * LEAVE CONTROLLER — 10 Endpoints
 * ============================================================
 *
 * @AuthenticationPrincipal UserDetails currentUser:
 *   Spring injects the currently authenticated user.
 *   Used to track who is performing the action:
 *   - Apply leave: currentUser.getUsername() = employee's email
 *   - Approve/Reject: currentUser.getUsername() = HR/ADMIN's email
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Leave application, approval, and balance APIs")
public class LeaveController {

    private final LeaveService leaveService;

    // ─── LEAVE TYPES ───────────────────────────────────────────────

    @Operation(summary = "Get all leave types")
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<LeaveTypeResponse>>> getAllLeaveTypes() {
        return ResponseEntity.ok(
                ApiResponse.success("Leave types fetched", leaveService.getAllLeaveTypes(), 200)
        );
    }

    @Operation(summary = "Create a new leave type (ADMIN only)")
    @PostMapping("/types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LeaveTypeResponse>> createLeaveType(
            @Valid @RequestBody LeaveTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave type created", leaveService.createLeaveType(request), 201));
    }

    // ─── LEAVE APPLICATIONS ────────────────────────────────────────

    @Operation(summary = "Apply for leave")
    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<LeaveResponse>> applyLeave(
            @Valid @RequestBody LeaveApplyRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        LeaveResponse response = leaveService.applyLeave(request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave application submitted", response, 201));
    }

    @Operation(summary = "Get all leave requests (paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<PageResponse<LeaveResponse>>> getAllLeaves(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageResponse<LeaveResponse> response =
                leaveService.getAllLeaves(PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success("Leaves fetched", response, 200));
    }

    @Operation(summary = "Get leave request by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<LeaveResponse>> getLeaveById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Leave fetched", leaveService.getLeaveById(id), 200)
        );
    }

    @Operation(summary = "Get leave requests by employee")
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<PageResponse<LeaveResponse>>> getLeavesByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<LeaveResponse> response = leaveService.getLeavesByEmployee(
                employeeId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success("Employee leaves fetched", response, 200));
    }

    @Operation(summary = "Approve a leave request (HR/ADMIN)")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<LeaveResponse>> approveLeave(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveActionRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        LeaveResponse response = leaveService.approveLeave(id, request, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Leave approved", response, 200));
    }

    @Operation(summary = "Reject a leave request (HR/ADMIN)")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<LeaveResponse>> rejectLeave(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveActionRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        LeaveResponse response = leaveService.rejectLeave(id, request, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Leave rejected", response, 200));
    }

    @Operation(summary = "Cancel a leave request")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> cancelLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        leaveService.cancelLeave(id, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Leave cancelled", 200));
    }

    // ─── LEAVE BALANCE ─────────────────────────────────────────────

    @Operation(summary = "Get leave balance for an employee")
    @GetMapping("/balance/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getLeaveBalance(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int year) {

        int targetYear = year == 0 ? Year.now().getValue() : year;
        List<LeaveBalanceResponse> response = leaveService.getLeaveBalance(employeeId, targetYear);
        return ResponseEntity.ok(ApiResponse.success("Leave balance fetched", response, 200));
    }

    @Operation(summary = "Initialize leave balance for an employee (HR/ADMIN)")
    @PostMapping("/balance/initialize/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<Void>> initializeLeaveBalance(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int year) {

        int targetYear = year == 0 ? Year.now().getValue() : year;
        leaveService.initializeLeaveBalance(employeeId, targetYear);
        return ResponseEntity.ok(ApiResponse.success("Leave balance initialized", 200));
    }
}
