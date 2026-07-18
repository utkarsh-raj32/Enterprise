package com.enterprise.hrm.department.service;

import com.enterprise.hrm.common.PageResponse;
import com.enterprise.hrm.department.dto.DepartmentRequest;
import com.enterprise.hrm.department.dto.DepartmentResponse;
import com.enterprise.hrm.department.entity.Department;
import com.enterprise.hrm.department.repository.DepartmentRepository;
import com.enterprise.hrm.employee.dto.EmployeeResponse;
import com.enterprise.hrm.employee.entity.Employee;
import com.enterprise.hrm.employee.repository.EmployeeRepository;
import com.enterprise.hrm.exception.BusinessException;
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
 * DEPARTMENT SERVICE IMPLEMENTATION
 * ============================================================
 *
 * Key design decisions:
 *   1. Entity-to-DTO conversion in service layer (not controller)
 *      Service returns DTOs — controller never sees entities
 *   2. @Transactional(readOnly=true) on all GET methods
 *      Better performance; Hibernate skips dirty checking
 *   3. Business rule enforcement here — not in controller or repo
 *
 * @Transactional(readOnly = true) on class level:
 *   All methods default to read-only. Write methods override with
 *   @Transactional (readOnly = false is the default).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        log.info("Creating department: {}", request.getName());

        if (departmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Department", "code", request.getCode());
        }
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }

        Department department = Department.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .managerName(request.getManagerName())
                .active(true)
                .build();

        Department saved = departmentRepository.save(department);
        log.info("Department created with ID: {}", saved.getId());
        return mapToResponse(saved, 0L);
    }

    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        Department dept = findDepartmentOrThrow(id);
        long empCount = departmentRepository.countEmployeesByDepartmentId(id);
        return mapToResponse(dept, empCount);
    }

    @Override
    public DepartmentResponse getDepartmentByCode(String code) {
        Department dept = departmentRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "code", code));
        long empCount = departmentRepository.countEmployeesByDepartmentId(dept.getId());
        return mapToResponse(dept, empCount);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(dept -> mapToResponse(dept,
                        departmentRepository.countEmployeesByDepartmentId(dept.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartmentResponse> getActiveDepartments() {
        return departmentRepository.findByActiveTrue()
                .stream()
                .map(dept -> mapToResponse(dept,
                        departmentRepository.countEmployeesByDepartmentId(dept.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        log.info("Updating department ID: {}", id);
        Department dept = findDepartmentOrThrow(id);

        // Check code uniqueness — only if code is changing
        if (!dept.getCode().equals(request.getCode()) &&
                departmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Department", "code", request.getCode());
        }

        dept.setName(request.getName());
        dept.setCode(request.getCode().toUpperCase());
        dept.setDescription(request.getDescription());
        dept.setManagerName(request.getManagerName());

        Department updated = departmentRepository.save(dept);
        long empCount = departmentRepository.countEmployeesByDepartmentId(id);
        return mapToResponse(updated, empCount);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        log.info("Deleting department ID: {}", id);
        Department dept = findDepartmentOrThrow(id);

        // Business rule: can't delete department with active employees
        long empCount = employeeRepository.countByDepartmentId(id);
        if (empCount > 0) {
            throw new BusinessException(
                String.format("Cannot delete department '%s' — it has %d active employee(s). " +
                              "Reassign employees first.", dept.getName(), empCount),
                "DEPT_HAS_EMPLOYEES"
            );
        }

        // Soft delete (mark inactive) instead of hard delete
        dept.setActive(false);
        departmentRepository.save(dept);
        log.info("Department soft-deleted: {}", dept.getName());
    }

    @Override
    public PageResponse<EmployeeResponse> getDepartmentEmployees(Long id, Pageable pageable) {
        // Verify department exists
        findDepartmentOrThrow(id);

        Page<Employee> employeePage = employeeRepository.findByDepartmentId(id, pageable);

        List<EmployeeResponse> employeeResponses = employeePage.getContent()
                .stream()
                .map(this::mapEmployeeToResponse)
                .collect(Collectors.toList());

        return PageResponse.<EmployeeResponse>builder()
                .content(employeeResponses)
                .pageNumber(employeePage.getNumber())
                .pageSize(employeePage.getSize())
                .totalElements(employeePage.getTotalElements())
                .totalPages(employeePage.getTotalPages())
                .last(employeePage.isLast())
                .first(employeePage.isFirst())
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS — Entity to DTO mapping
    // ─────────────────────────────────────────────────────────────

    private Department findDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    private DepartmentResponse mapToResponse(Department dept, long empCount) {
        return DepartmentResponse.builder()
                .id(dept.getId())
                .name(dept.getName())
                .code(dept.getCode())
                .description(dept.getDescription())
                .managerName(dept.getManagerName())
                .active(dept.isActive())
                .employeeCount(empCount)
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt())
                .build();
    }

    private EmployeeResponse mapEmployeeToResponse(Employee emp) {
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
                .build();
    }
}
