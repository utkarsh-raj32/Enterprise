package com.enterprise.hrm.employee.service;

import com.enterprise.hrm.common.PageResponse;
import com.enterprise.hrm.department.entity.Department;
import com.enterprise.hrm.department.repository.DepartmentRepository;
import com.enterprise.hrm.employee.dto.EmployeeRequest;
import com.enterprise.hrm.employee.dto.EmployeeResponse;
import com.enterprise.hrm.employee.entity.Employee;
import com.enterprise.hrm.employee.entity.EmployeeStatus;
import com.enterprise.hrm.employee.repository.EmployeeRepository;
import com.enterprise.hrm.exception.DuplicateResourceException;
import com.enterprise.hrm.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * EMPLOYEE SERVICE IMPLEMENTATION
 * ============================================================
 *
 * Key patterns used:
 * 1. MAPPER PATTERN — private mapToResponse() converts Entity → DTO
 *    Never leak entities to the API layer.
 *
 * 2. GUARD CLAUSES — check existence and throw early, reducing nesting
 *
 * 3. STREAMS API — functional-style collection processing for mapping
 *
 * 4. @Transactional(readOnly=true) on class — all reads are optimized
 *    Write methods override with @Transactional
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("Creating employee: {} {}", request.getFirstName(), request.getLastName());

        // Guard clauses — fail fast on business rule violations
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Employee", "email", request.getEmail());
        }
        if (employeeRepository.existsByEmpCode(request.getEmpCode())) {
            throw new DuplicateResourceException("Employee", "empCode", request.getEmpCode());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id",
                        request.getDepartmentId()));

        Employee employee = Employee.builder()
                .empCode(request.getEmpCode())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .designation(request.getDesignation())
                .joiningDate(request.getJoiningDate())
                .department(department)
                .salary(request.getSalary())
                .address(request.getAddress())
                .status(request.getStatus() != null ? request.getStatus() : EmployeeStatus.ACTIVE)
                .build();

        Employee saved = employeeRepository.save(employee);
        log.info("Employee created with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        return mapToResponse(findEmployeeOrThrow(id));
    }

    @Override
    public EmployeeResponse getEmployeeByCode(String empCode) {
        Employee employee = employeeRepository.findByEmpCode(empCode)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "empCode", empCode));
        return mapToResponse(employee);
    }

    @Override
    public PageResponse<EmployeeResponse> getAllEmployees(Pageable pageable) {
        Page<Employee> page = employeeRepository.findAll(pageable);
        return buildPageResponse(page);
    }

    @Override
    public PageResponse<EmployeeResponse> searchEmployees(String query, Long departmentId,
                                                           EmployeeStatus status, Pageable pageable) {
        Page<Employee> page = employeeRepository.searchEmployees(query, departmentId, status, pageable);
        return buildPageResponse(page);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        log.info("Updating employee ID: {}", id);
        Employee employee = findEmployeeOrThrow(id);

        // Check email uniqueness only if changing
        if (!employee.getEmail().equals(request.getEmail()) &&
                employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Employee", "email", request.getEmail());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id",
                        request.getDepartmentId()));

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDesignation(request.getDesignation());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setDepartment(department);
        employee.setSalary(request.getSalary());
        employee.setAddress(request.getAddress());
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }

        return mapToResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        log.info("Soft-deleting employee ID: {}", id);
        Employee employee = findEmployeeOrThrow(id);
        // Soft delete — mark as TERMINATED, don't remove from DB
        // This preserves historical data (leave records, attendance, payslips)
        employee.setStatus(EmployeeStatus.TERMINATED);
        employeeRepository.save(employee);
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    private Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    private PageResponse<EmployeeResponse> buildPageResponse(Page<Employee> page) {
        List<EmployeeResponse> content = page.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<EmployeeResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    /**
     * ENTITY → DTO MAPPING
     * Centralized mapping — if entity changes, update here only.
     * In large projects: use MapStruct for compile-time safe mapping.
     */
    private EmployeeResponse mapToResponse(Employee emp) {
        EmployeeResponse.DepartmentSummary deptSummary = null;
        if (emp.getDepartment() != null) {
            deptSummary = EmployeeResponse.DepartmentSummary.builder()
                    .id(emp.getDepartment().getId())
                    .name(emp.getDepartment().getName())
                    .code(emp.getDepartment().getCode())
                    .build();
        }

        return EmployeeResponse.builder()
                .id(emp.getId())
                .empCode(emp.getEmpCode())
                .firstName(emp.getFirstName())
                .lastName(emp.getLastName())
                .fullName(emp.getFirstName() + " " + emp.getLastName())
                .email(emp.getEmail())
                .phone(emp.getPhone())
                .designation(emp.getDesignation())
                .joiningDate(emp.getJoiningDate())
                .status(emp.getStatus())
                .salary(emp.getSalary())
                .address(emp.getAddress())
                .department(deptSummary)
                .createdAt(emp.getCreatedAt())
                .updatedAt(emp.getUpdatedAt())
                .build();
    }
}
