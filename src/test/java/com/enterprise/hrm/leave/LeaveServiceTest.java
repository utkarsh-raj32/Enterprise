package com.enterprise.hrm.leave;

import com.enterprise.hrm.auth.repository.UserRepository;
import com.enterprise.hrm.employee.entity.Employee;
import com.enterprise.hrm.employee.repository.EmployeeRepository;
import com.enterprise.hrm.exception.BusinessException;
import com.enterprise.hrm.exception.ResourceNotFoundException;
import com.enterprise.hrm.leave.dto.LeaveApplyRequest;
import com.enterprise.hrm.leave.dto.LeaveResponse;
import com.enterprise.hrm.leave.entity.*;
import com.enterprise.hrm.leave.repository.LeaveBalanceRepository;
import com.enterprise.hrm.leave.repository.LeaveRequestRepository;
import com.enterprise.hrm.leave.repository.LeaveTypeRepository;
import com.enterprise.hrm.leave.service.LeaveServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Leave Service Unit Tests.
 * Focuses on business rule enforcement in the leave application flow.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LeaveService Unit Tests")
class LeaveServiceTest {

    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private LeaveTypeRepository leaveTypeRepository;
    @Mock private LeaveBalanceRepository leaveBalanceRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    private Employee mockEmployee;
    private LeaveType mockLeaveType;
    private LeaveBalance mockBalance;
    private LeaveApplyRequest applyRequest;

    @BeforeEach
    void setUp() {
        mockEmployee = Employee.builder()
                .id(1L)
                .empCode("EMP-001")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@enterprise.com")
                .build();

        mockLeaveType = LeaveType.builder()
                .id(1L)
                .name("Annual Leave")
                .maxDaysPerYear(21)
                .paid(true)
                .active(true)
                .build();

        mockBalance = LeaveBalance.builder()
                .id(1L)
                .employee(mockEmployee)
                .leaveType(mockLeaveType)
                .year(LocalDate.now().getYear())
                .totalDays(21)
                .usedDays(5)
                .remainingDays(16)
                .build();

        applyRequest = new LeaveApplyRequest();
        applyRequest.setEmployeeId(1L);
        applyRequest.setLeaveTypeId(1L);
        applyRequest.setStartDate(LocalDate.now().plusDays(1));
        applyRequest.setEndDate(LocalDate.now().plusDays(3));
        applyRequest.setReason("Family vacation — pre-planned annual leave");
    }

    @Nested
    @DisplayName("applyLeave()")
    class ApplyLeaveTests {

        @Test
        @DisplayName("Should apply leave successfully when balance is sufficient")
        void shouldApplyLeaveSuccessfully() {
            // ARRANGE
            LeaveRequest savedRequest = LeaveRequest.builder()
                    .id(1L)
                    .employee(mockEmployee)
                    .leaveType(mockLeaveType)
                    .startDate(applyRequest.getStartDate())
                    .endDate(applyRequest.getEndDate())
                    .numberOfDays(3)
                    .reason(applyRequest.getReason())
                    .status(LeaveStatus.PENDING)
                    .build();

            given(employeeRepository.findById(1L)).willReturn(Optional.of(mockEmployee));
            given(leaveTypeRepository.findById(1L)).willReturn(Optional.of(mockLeaveType));
            given(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, any()))
                    .willReturn(Optional.of(mockBalance));
            given(leaveRequestRepository.findOverlappingLeaves(any(), any(), any()))
                    .willReturn(Collections.emptyList());
            given(leaveRequestRepository.save(any(LeaveRequest.class))).willReturn(savedRequest);

            // ACT
            LeaveResponse response = leaveService.applyLeave(applyRequest, "jane@enterprise.com");

            // ASSERT
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(LeaveStatus.PENDING);
            assertThat(response.getNumberOfDays()).isEqualTo(3);
            assertThat(response.getEmployeeName()).isEqualTo("Jane Smith");
        }

        @Test
        @DisplayName("Should throw BusinessException when end date is before start date")
        void shouldThrowWhenEndDateBeforeStartDate() {
            applyRequest.setStartDate(LocalDate.now().plusDays(5));
            applyRequest.setEndDate(LocalDate.now().plusDays(2));  // Before start date

            assertThatThrownBy(() -> leaveService.applyLeave(applyRequest, "jane@enterprise.com"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("End date cannot be before start date");
        }

        @Test
        @DisplayName("Should throw BusinessException when leave balance is insufficient")
        void shouldThrowWhenBalanceInsufficient() {
            // Only 2 days remaining, but requesting 3
            mockBalance.setRemainingDays(2);

            given(employeeRepository.findById(1L)).willReturn(Optional.of(mockEmployee));
            given(leaveTypeRepository.findById(1L)).willReturn(Optional.of(mockLeaveType));
            given(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(any(), any(), any()))
                    .willReturn(Optional.of(mockBalance));

            assertThatThrownBy(() -> leaveService.applyLeave(applyRequest, "jane@enterprise.com"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Insufficient leave balance");
        }

        @Test
        @DisplayName("Should throw BusinessException when leave dates overlap")
        void shouldThrowWhenDatesOverlap() {
            LeaveRequest existingLeave = LeaveRequest.builder()
                    .id(99L)
                    .status(LeaveStatus.APPROVED)
                    .build();

            given(employeeRepository.findById(1L)).willReturn(Optional.of(mockEmployee));
            given(leaveTypeRepository.findById(1L)).willReturn(Optional.of(mockLeaveType));
            given(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(any(), any(), any()))
                    .willReturn(Optional.of(mockBalance));
            given(leaveRequestRepository.findOverlappingLeaves(any(), any(), any()))
                    .willReturn(Collections.singletonList(existingLeave));

            assertThatThrownBy(() -> leaveService.applyLeave(applyRequest, "jane@enterprise.com"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("overlap");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when employee not found")
        void shouldThrowWhenEmployeeNotFound() {
            given(employeeRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> leaveService.applyLeave(applyRequest, "jane@enterprise.com"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee");
        }
    }
}
