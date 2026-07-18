package com.enterprise.hrm.employee.controller;

import com.enterprise.hrm.common.ApiResponse;
import com.enterprise.hrm.common.PageResponse;
import com.enterprise.hrm.employee.dto.EmployeeRequest;
import com.enterprise.hrm.employee.dto.EmployeeResponse;
import com.enterprise.hrm.employee.entity.EmployeeStatus;
import com.enterprise.hrm.employee.service.EmployeeService;
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
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================
 * EMPLOYEE CONTROLLER — 9 Endpoints
 * ============================================================
 *
 * REST API Design Principles followed:
 * • Nouns in URLs — /employees (not /getEmployees)
 * • HTTP verbs for actions — GET, POST, PUT, DELETE
 * • Plural resource names — /employees (not /employee)
 * • Nested resources for relationships — /employees/{id}/leaves
 * • Query params for filtering/pagination — ?page=0&size=10
 * • Consistent response structure via ApiResponse<T>
 * • Proper HTTP status codes — 200, 201, 204, 400, 404, 409
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management APIs")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "Create a new employee")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", response, 201));
    }

    @Operation(summary = "Get all employees with pagination and sorting")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getAllEmployees(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageResponse<EmployeeResponse> response =
                employeeService.getAllEmployees(PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.success("Employees fetched", response, 200));
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Employee fetched", employeeService.getEmployeeById(id), 200)
        );
    }

    @Operation(summary = "Get employee by employee code")
    @GetMapping("/code/{empCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeByCode(
            @PathVariable String empCode) {
        return ResponseEntity.ok(
                ApiResponse.success("Employee fetched",
                        employeeService.getEmployeeByCode(empCode), 200)
        );
    }

    @Operation(summary = "Search employees by name, email, designation, department, or status")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> searchEmployees(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageResponse<EmployeeResponse> response = employeeService.searchEmployees(
                query, departmentId, status, PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.success("Search results", response, 200));
    }

    @Operation(summary = "Update employee")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated", response, 200));
    }

    @Operation(summary = "Delete employee (soft delete — marks as TERMINATED)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted", 200));
    }
}
