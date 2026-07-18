package com.enterprise.hrm.employee;

import com.enterprise.hrm.department.entity.Department;
import com.enterprise.hrm.department.repository.DepartmentRepository;
import com.enterprise.hrm.employee.dto.EmployeeRequest;
import com.enterprise.hrm.employee.dto.EmployeeResponse;
import com.enterprise.hrm.employee.entity.Employee;
import com.enterprise.hrm.employee.entity.EmployeeStatus;
import com.enterprise.hrm.employee.repository.EmployeeRepository;
import com.enterprise.hrm.employee.service.EmployeeServiceImpl;
import com.enterprise.hrm.exception.DuplicateResourceException;
import com.enterprise.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * ============================================================
 * EMPLOYEE SERVICE UNIT TESTS — JUnit 5 + Mockito
 * ============================================================
 *
 * @ExtendWith(MockitoExtension.class):
 *   Integrates Mockito with JUnit 5.
 *   Replaces the older @RunWith(MockitoJUnitRunner.class) from JUnit 4.
 *   Automatically processes @Mock, @InjectMocks, @Captor etc.
 *
 * @Mock:
 *   Creates a Mockito mock object — a fake implementation that
 *   records interactions and can be configured to return specific values.
 *   WHY mock?
 *   We want to test EmployeeServiceImpl in ISOLATION from:
 *   - Database (EmployeeRepository)
 *   - Other services (DepartmentRepository)
 *   This makes tests fast (no DB), deterministic, and focused.
 *
 * @InjectMocks:
 *   Creates an instance of EmployeeServiceImpl and injects all @Mock
 *   fields into it via constructor/field injection.
 *   Result: a real EmployeeServiceImpl with fake dependencies.
 *
 * UNIT TEST PHILOSOPHY (AAA Pattern):
 *   Arrange — set up test data and mock behavior
 *   Act     — call the method under test
 *   Assert  — verify the outcome
 *
 * BDD-STYLE (given/when/then with Mockito BDDMockito):
 *   given(mock.method()).willReturn(value) — more readable than when/then
 *   willThrow(), willDoNothing(), etc.
 *
 * AssertJ (assertThat):
 *   Fluent assertion library included with Spring Boot Test.
 *   More readable than JUnit's assertEquals:
 *   assertThat(actual).isEqualTo(expected).isNotNull()
 *
 * @Nested:
 *   Groups related tests inside a class. Improves organization.
 *   Inner class tests share setup from outer class.
 *
 * @DisplayName:
 *   Human-readable test name shown in IDE/test reports.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Unit Tests")
class EmployeeServiceTest {

    // ─── MOCKS ────────────────────────────────────────────────────

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    // ─── SYSTEM UNDER TEST ────────────────────────────────────────

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    // ─── TEST FIXTURES ────────────────────────────────────────────

    private Department mockDepartment;
    private Employee mockEmployee;
    private EmployeeRequest createRequest;

    /**
     * @BeforeEach — runs before EACH test method.
     * Initializes shared test data to avoid duplication.
     * WHY not @BeforeAll? @BeforeAll requires static and shares state
     * between tests — can cause test interference.
     */
    @BeforeEach
    void setUp() {
        mockDepartment = Department.builder()
                .id(1L)
                .name("Engineering")
                .code("ENG")
                .active(true)
                .build();

        mockEmployee = Employee.builder()
                .id(1L)
                .empCode("EMP-001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@enterprise.com")
                .phone("+911234567890")
                .designation("Software Engineer")
                .joiningDate(LocalDate.of(2023, 1, 15))
                .department(mockDepartment)
                .salary(new BigDecimal("75000.00"))
                .status(EmployeeStatus.ACTIVE)
                .build();

        createRequest = new EmployeeRequest();
        createRequest.setEmpCode("EMP-001");
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setEmail("john.doe@enterprise.com");
        createRequest.setPhone("+911234567890");
        createRequest.setDesignation("Software Engineer");
        createRequest.setJoiningDate(LocalDate.of(2023, 1, 15));
        createRequest.setDepartmentId(1L);
        createRequest.setSalary(new BigDecimal("75000.00"));
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE EMPLOYEE TESTS
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createEmployee()")
    class CreateEmployeeTests {

        @Test
        @DisplayName("Should create employee successfully when all data is valid")
        void shouldCreateEmployeeSuccessfully() {
            // ARRANGE
            given(employeeRepository.existsByEmail(anyString())).willReturn(false);
            given(employeeRepository.existsByEmpCode(anyString())).willReturn(false);
            given(departmentRepository.findById(1L)).willReturn(Optional.of(mockDepartment));
            given(employeeRepository.save(any(Employee.class))).willReturn(mockEmployee);

            // ACT
            EmployeeResponse response = employeeService.createEmployee(createRequest);

            // ASSERT
            assertThat(response).isNotNull();
            assertThat(response.getEmpCode()).isEqualTo("EMP-001");
            assertThat(response.getEmail()).isEqualTo("john.doe@enterprise.com");
            assertThat(response.getFullName()).isEqualTo("John Doe");
            assertThat(response.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);

            // Verify interactions
            then(employeeRepository).should().save(any(Employee.class));
            then(departmentRepository).should().findById(1L);
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void shouldThrowExceptionWhenEmailDuplicate() {
            // ARRANGE
            given(employeeRepository.existsByEmail("john.doe@enterprise.com")).willReturn(true);

            // ACT & ASSERT
            assertThatThrownBy(() -> employeeService.createEmployee(createRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");

            // Verify that save was NEVER called
            then(employeeRepository).should(never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when emp code already exists")
        void shouldThrowExceptionWhenEmpCodeDuplicate() {
            // ARRANGE
            given(employeeRepository.existsByEmail(anyString())).willReturn(false);
            given(employeeRepository.existsByEmpCode("EMP-001")).willReturn(true);

            // ACT & ASSERT
            assertThatThrownBy(() -> employeeService.createEmployee(createRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("empCode");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when department not found")
        void shouldThrowExceptionWhenDepartmentNotFound() {
            // ARRANGE
            given(employeeRepository.existsByEmail(anyString())).willReturn(false);
            given(employeeRepository.existsByEmpCode(anyString())).willReturn(false);
            given(departmentRepository.findById(1L)).willReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> employeeService.createEmployee(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Department");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET EMPLOYEE TESTS
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getEmployeeById()")
    class GetEmployeeTests {

        @Test
        @DisplayName("Should return employee when ID exists")
        void shouldReturnEmployeeWhenIdExists() {
            // ARRANGE
            given(employeeRepository.findById(1L)).willReturn(Optional.of(mockEmployee));

            // ACT
            EmployeeResponse response = employeeService.getEmployeeById(1L);

            // ASSERT
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getEmpCode()).isEqualTo("EMP-001");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when ID does not exist")
        void shouldThrowExceptionWhenIdNotFound() {
            // ARRANGE
            given(employeeRepository.findById(999L)).willReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee")
                    .hasMessageContaining("999");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE EMPLOYEE TESTS (Soft Delete)
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteEmployee() — Soft Delete")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("Should mark employee as TERMINATED (soft delete)")
        void shouldSoftDeleteEmployee() {
            // ARRANGE
            given(employeeRepository.findById(1L)).willReturn(Optional.of(mockEmployee));
            given(employeeRepository.save(any(Employee.class))).willReturn(mockEmployee);

            // ACT
            employeeService.deleteEmployee(1L);

            // ASSERT — verify save was called (for soft delete update)
            then(employeeRepository).should().save(argThat(emp ->
                    emp.getStatus() == EmployeeStatus.TERMINATED
            ));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when employee to delete not found")
        void shouldThrowWhenEmployeeNotFoundForDelete() {
            // ARRANGE
            given(employeeRepository.findById(999L)).willReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> employeeService.deleteEmployee(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
