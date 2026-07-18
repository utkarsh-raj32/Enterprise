package com.enterprise.hrm.leave.service;

import com.enterprise.hrm.common.PageResponse;
import com.enterprise.hrm.leave.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Leave service interface — defines all leave management operations.
 */
public interface LeaveService {

    // Leave Types
    LeaveTypeResponse createLeaveType(LeaveTypeRequest request);
    List<LeaveTypeResponse> getAllLeaveTypes();

    // Leave Applications
    LeaveResponse applyLeave(LeaveApplyRequest request, String currentUserEmail);
    LeaveResponse getLeaveById(Long id);
    PageResponse<LeaveResponse> getAllLeaves(Pageable pageable);
    PageResponse<LeaveResponse> getLeavesByEmployee(Long employeeId, Pageable pageable);
    LeaveResponse approveLeave(Long leaveId, LeaveActionRequest request, String approverEmail);
    LeaveResponse rejectLeave(Long leaveId, LeaveActionRequest request, String approverEmail);
    void cancelLeave(Long leaveId, String currentUserEmail);

    // Leave Balance
    List<LeaveBalanceResponse> getLeaveBalance(Long employeeId, int year);
    void initializeLeaveBalance(Long employeeId, int year);
}
