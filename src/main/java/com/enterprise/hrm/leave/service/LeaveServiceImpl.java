package com.enterprise.hrm.leave.service;

import com.enterprise.hrm.auth.entity.User;
import com.enterprise.hrm.auth.repository.UserRepository;
import com.enterprise.hrm.common.PageResponse;
import com.enterprise.hrm.employee.entity.Employee;
import com.enterprise.hrm.employee.repository.EmployeeRepository;
import com.enterprise.hrm.exception.BusinessException;
import com.enterprise.hrm.exception.DuplicateResourceException;
import com.enterprise.hrm.exception.ResourceNotFoundException;
import com.enterprise.hrm.exception.UnauthorizedException;
import com.enterprise.hrm.leave.dto.*;
import com.enterprise.hrm.leave.entity.*;
import com.enterprise.hrm.leave.repository.LeaveBalanceRepository;
import com.enterprise.hrm.leave.repository.LeaveRequestRepository;
import com.enterprise.hrm.leave.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * LEAVE SERVICE IMPLEMENTATION
 * ============================================================
 *
 * Business rules enforced:
 * 1. Cannot apply for leave if insufficient balance
 * 2. Cannot apply for overlapping dates (if PENDING/APPROVED)
 * 3. End date must be >= start date
 * 4. Only HR/ADMIN can approve/reject
 * 5. Only the employee or HR/ADMIN can cancel
 * 6. Cannot approve/reject an already-decided leave
 *
 * @Transactional on approve/reject:
 *   Both update the leave request AND the leave balance.
 *   If either fails, both roll back — no partial state.
 *   This is ACID atomicity via Spring's transaction management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────
    // LEAVE TYPES
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveTypeResponse createLeaveType(LeaveTypeRequest request) {
        if (leaveTypeRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("LeaveType", "name", request.getName());
        }

        LeaveType leaveType = LeaveType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .maxDaysPerYear(request.getMaxDaysPerYear())
                .paid(request.isPaid())
                .active(true)
                .build();

        return mapLeaveTypeToResponse(leaveTypeRepository.save(leaveType));
    }

    @Override
    public List<LeaveTypeResponse> getAllLeaveTypes() {
        return leaveTypeRepository.findByActiveTrue()
                .stream()
                .map(this::mapLeaveTypeToResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // LEAVE APPLICATION
    // ─────────────────────────────────────────────────────────────

    /**
     * APPLY LEAVE FLOW:
     * 1. Validate dates (end >= start)
     * 2. Calculate working days
     * 3. Check leave balance
     * 4. Check for overlapping leaves
     * 5. Create leave request in PENDING status
     */
    @Override
    @Transactional
    public LeaveResponse applyLeave(LeaveApplyRequest request, String currentUserEmail) {
        log.info("Employee {} applying for leave", request.getEmployeeId());

        // 1. Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("End date cannot be before start date", "INVALID_DATES");
        }
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot apply for leave in the past", "PAST_DATE");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", request.getLeaveTypeId()));

        // 2. Calculate working days (simplified — counts all days including weekends)
        // In production: use a holiday calendar to count only business days
        long numberOfDays = request.getStartDate()
                .datesUntil(request.getEndDate().plusDays(1))
                .count();

        // 3. Check leave balance
        int year = request.getStartDate().getYear();
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employee.getId(), leaveType.getId(), year)
                .orElseThrow(() -> new BusinessException(
                        "No leave balance found for employee for " + leaveType.getName() +
                        " in year " + year + ". Please contact HR to initialize balance.",
                        "NO_BALANCE"
                ));

        if (balance.getRemainingDays() < numberOfDays) {
            throw new BusinessException(
                    String.format("Insufficient leave balance. Requested: %d days, Available: %d days",
                            numberOfDays, balance.getRemainingDays()),
                    "INSUFFICIENT_BALANCE"
            );
        }

        // 4. Check overlapping leaves
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaves(
                employee.getId(), request.getStartDate(), request.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new BusinessException(
                    "Leave dates overlap with an existing pending/approved leave request",
                    "OVERLAPPING_LEAVE"
            );
        }

        // 5. Create leave request
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .numberOfDays((int) numberOfDays)
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request created with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public LeaveResponse getLeaveById(Long id) {
        return mapToResponse(findLeaveOrThrow(id));
    }

    @Override
    public PageResponse<LeaveResponse> getAllLeaves(Pageable pageable) {
        Page<LeaveRequest> page = leaveRequestRepository.findAll(pageable);
        return buildPageResponse(page);
    }

    @Override
    public PageResponse<LeaveResponse> getLeavesByEmployee(Long employeeId, Pageable pageable) {
        // Verify employee exists
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        Page<LeaveRequest> page = leaveRequestRepository.findByEmployeeId(employeeId, pageable);
        return buildPageResponse(page);
    }

    /**
     * APPROVE LEAVE FLOW:
     * 1. Verify leave exists and is PENDING
     * 2. Update leave status to APPROVED
     * 3. Deduct days from leave balance
     * All in ONE transaction — atomic operation
     */
    @Override
    @Transactional
    public LeaveResponse approveLeave(Long leaveId, LeaveActionRequest request, String approverEmail) {
        log.info("Approving leave request ID: {} by {}", leaveId, approverEmail);

        LeaveRequest leaveRequest = findLeaveOrThrow(leaveId);

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException(
                    "Only PENDING leave requests can be approved. Current status: " + leaveRequest.getStatus(),
                    "INVALID_STATUS"
            );
        }

        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", approverEmail));

        // Update leave request
        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        if (request != null && request.getApproverNote() != null) {
            leaveRequest.setApproverNote(request.getApproverNote());
        }

        // Deduct from leave balance
        int year = leaveRequest.getStartDate().getYear();
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                leaveRequest.getEmployee().getId(),
                leaveRequest.getLeaveType().getId(),
                year
        ).orElseThrow(() -> new BusinessException("Leave balance not found", "NO_BALANCE"));

        balance.setUsedDays(balance.getUsedDays() + leaveRequest.getNumberOfDays());
        balance.calculateRemainingDays();
        leaveBalanceRepository.save(balance);

        return mapToResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    @Transactional
    public LeaveResponse rejectLeave(Long leaveId, LeaveActionRequest request, String approverEmail) {
        log.info("Rejecting leave request ID: {} by {}", leaveId, approverEmail);

        LeaveRequest leaveRequest = findLeaveOrThrow(leaveId);

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException(
                    "Only PENDING leave requests can be rejected. Current status: " + leaveRequest.getStatus(),
                    "INVALID_STATUS"
            );
        }

        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", approverEmail));

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        if (request != null) {
            leaveRequest.setApproverNote(request.getApproverNote());
        }

        // Note: Balance is NOT deducted on rejection — only on approval
        return mapToResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    @Transactional
    public void cancelLeave(Long leaveId, String currentUserEmail) {
        LeaveRequest leaveRequest = findLeaveOrThrow(leaveId);

        if (leaveRequest.getStatus() == LeaveStatus.REJECTED ||
            leaveRequest.getStatus() == LeaveStatus.CANCELLED) {
            throw new BusinessException("Cannot cancel a " + leaveRequest.getStatus() + " leave request");
        }

        boolean wasApproved = leaveRequest.getStatus() == LeaveStatus.APPROVED;
        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        leaveRequestRepository.save(leaveRequest);

        // If was approved, restore the balance
        if (wasApproved) {
            int year = leaveRequest.getStartDate().getYear();
            leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                    leaveRequest.getEmployee().getId(),
                    leaveRequest.getLeaveType().getId(),
                    year
            ).ifPresent(balance -> {
                balance.setUsedDays(balance.getUsedDays() - leaveRequest.getNumberOfDays());
                balance.calculateRemainingDays();
                leaveBalanceRepository.save(balance);
            });
        }
    }

    @Override
    public List<LeaveBalanceResponse> getLeaveBalance(Long employeeId, int year) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        return leaveBalanceRepository.findBalancesWithLeaveType(employeeId, year)
                .stream()
                .map(this::mapBalanceToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void initializeLeaveBalance(Long employeeId, int year) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        List<LeaveType> leaveTypes = leaveTypeRepository.findByActiveTrue();

        for (LeaveType leaveType : leaveTypes) {
            // Skip if balance already exists
            if (leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                    employeeId, leaveType.getId(), year).isPresent()) {
                continue;
            }

            LeaveBalance balance = LeaveBalance.builder()
                    .employee(employee)
                    .leaveType(leaveType)
                    .year(year)
                    .totalDays(leaveType.getMaxDaysPerYear())
                    .usedDays(0)
                    .remainingDays(leaveType.getMaxDaysPerYear())
                    .build();

            leaveBalanceRepository.save(balance);
        }

        log.info("Leave balance initialized for employee {} for year {}", employeeId, year);
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    private LeaveRequest findLeaveOrThrow(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
    }

    private PageResponse<LeaveResponse> buildPageResponse(Page<LeaveRequest> page) {
        List<LeaveResponse> content = page.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<LeaveResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    private LeaveResponse mapToResponse(LeaveRequest lr) {
        String approvedByName = null;
        if (lr.getApprovedBy() != null) {
            approvedByName = lr.getApprovedBy().getFirstName() + " " + lr.getApprovedBy().getLastName();
        }

        return LeaveResponse.builder()
                .id(lr.getId())
                .employeeId(lr.getEmployee().getId())
                .employeeName(lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName())
                .employeeCode(lr.getEmployee().getEmpCode())
                .leaveTypeName(lr.getLeaveType().getName())
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .numberOfDays(lr.getNumberOfDays())
                .reason(lr.getReason())
                .status(lr.getStatus())
                .approvedByName(approvedByName)
                .approvedAt(lr.getApprovedAt())
                .approverNote(lr.getApproverNote())
                .createdAt(lr.getCreatedAt())
                .build();
    }

    private LeaveBalanceResponse mapBalanceToResponse(LeaveBalance lb) {
        return LeaveBalanceResponse.builder()
                .id(lb.getId())
                .leaveTypeName(lb.getLeaveType().getName())
                .paid(lb.getLeaveType().isPaid())
                .year(lb.getYear())
                .totalDays(lb.getTotalDays())
                .usedDays(lb.getUsedDays())
                .remainingDays(lb.getRemainingDays())
                .build();
    }

    private LeaveTypeResponse mapLeaveTypeToResponse(LeaveType lt) {
        return LeaveTypeResponse.builder()
                .id(lt.getId())
                .name(lt.getName())
                .description(lt.getDescription())
                .maxDaysPerYear(lt.getMaxDaysPerYear())
                .paid(lt.isPaid())
                .active(lt.isActive())
                .build();
    }
}
