package com.enterprise.hrm.department.controller;

import com.enterprise.hrm.common.ApiResponse;
import com.enterprise.hrm.common.PageResponse;
import com.enterprise.hrm.department.dto.DepartmentRequest;
import com.enterprise.hrm.department.dto.DepartmentResponse;
import com.enterprise.hrm.department.service.DepartmentService;
import com.enterprise.hrm.employee.dto.EmployeeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================
 * DEPARTMENT CONTROLLER
 * ============================================================
 *
 * @PreAuthorize:
 *   Method-level security via Spring Security SpEL expressions.
 *   Enabled by @EnableMethodSecurity in SecurityConfig.
 *
 *   Examples:
 *   @PreAuthorize("hasRole('ADMIN')")           — ADMIN only
 *   @PreAuthorize("hasAnyRole('ADMIN','HR')")   — ADMIN or HR
 *   @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
 *     — ADMIN or the user themselves
 *
 *   WHY @PreAuthorize over URL patterns in SecurityConfig?
 *   More granular — can use complex SpEL expressions.
 *   Closer to the code — easier to review permissions alongside logic.
 *   Works with @Service methods too — not just controllers.
 *
 * @PathVariable:
 *   Binds a URI path segment to a method parameter.
 *   GET /departments/{id} → @PathVariable Long id
 *
 * @RequestParam:
 *   Binds a query parameter to a method parameter.
 *   GET /departments?page=0&size=10&sort=name
 *   defaultValue — used when param is not provided
 *   required = false — param is optional
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Department management APIs")
public class DepartmentController {

    private final DepartmentService departmentService;

    // ─────────────────────────────────────────────────────────────
    // POST /api/v1/departments — Create department
    // ─────────────────────────────────────────────────────────────

    @Operation(summary = "Create a new department")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {

        log.info("POST /api/v1/departments - Creating: {}", request.getName());
        DepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created successfully", response, 201));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/departments — Get all departments
    // ─────────────────────────────────────────────────────────────

    @Operation(summary = "Get all departments")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {

        List<DepartmentResponse> departments = activeOnly
                ? departmentService.getActiveDepartments()
                : departmentService.getAllDepartments();

        return ResponseEntity.ok(
                ApiResponse.success("Departments fetched successfully", departments, 200)
        );
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/departments/{id} — Get department by ID
    // ─────────────────────────────────────────────────────────────

    @Operation(summary = "Get department by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(
            @Parameter(description = "Department ID") @PathVariable Long id) {

        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Department fetched successfully", response, 200)
        );
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /api/v1/departments/{id} — Update department
    // ─────────────────────────────────────────────────────────────

    @Operation(summary = "Update department")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {

        log.info("PUT /api/v1/departments/{}", id);
        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Department updated successfully", response, 200)
        );
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE /api/v1/departments/{id} — Soft delete department
    // ─────────────────────────────────────────────────────────────

    @Operation(summary = "Delete department (soft delete)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        log.info("DELETE /api/v1/departments/{}", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(
                ApiResponse.success("Department deleted successfully", 200)
        );
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/v1/departments/{id}/employees — Get dept employees (paginated)
    // ─────────────────────────────────────────────────────────────

    @Operation(summary = "Get employees of a department with pagination")
    @GetMapping("/{id}/employees")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getDepartmentEmployees(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<EmployeeResponse> response = departmentService.getDepartmentEmployees(id, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Department employees fetched", response, 200)
        );
    }
}
