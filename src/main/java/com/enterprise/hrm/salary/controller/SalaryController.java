package com.enterprise.hrm.salary.controller;

import com.enterprise.hrm.common.ApiResponse;
import com.enterprise.hrm.salary.dto.*;
import com.enterprise.hrm.salary.service.SalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Salary controller — 7 endpoints.
 * All endpoints restricted to ADMIN role for payroll security.
 */
@RestController
@RequestMapping("/api/v1/salary")
@RequiredArgsConstructor
@Tag(name = "Salary", description = "Salary structure and payslip management APIs")
@PreAuthorize("hasRole('ADMIN')")
public class SalaryController {

    private final SalaryService salaryService;

    @Operation(summary = "Create salary structure for employee")
    @PostMapping("/structure")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> createSalaryStructure(
            @Valid @RequestBody SalaryStructureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Salary structure created",
                        salaryService.createSalaryStructure(request), 201));
    }

    @Operation(summary = "Get current salary structure for employee")
    @GetMapping("/structure/{employeeId}")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> getCurrentSalaryStructure(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                ApiResponse.success("Salary structure fetched",
                        salaryService.getCurrentSalaryStructure(employeeId), 200)
        );
    }

    @Operation(summary = "Get salary history for employee")
    @GetMapping("/structure/{employeeId}/history")
    public ResponseEntity<ApiResponse<List<SalaryStructureResponse>>> getSalaryHistory(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                ApiResponse.success("Salary history fetched",
                        salaryService.getSalaryHistory(employeeId), 200)
        );
    }

    @Operation(summary = "Update salary structure")
    @PutMapping("/structure/{id}")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> updateSalaryStructure(
            @PathVariable Long id,
            @Valid @RequestBody SalaryStructureRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Salary structure updated",
                        salaryService.updateSalaryStructure(id, request), 200)
        );
    }

    @Operation(summary = "Generate payslip for employee")
    @PostMapping("/payslip/generate")
    public ResponseEntity<ApiResponse<PayslipResponse>> generatePayslip(
            @Valid @RequestBody PayslipGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payslip generated",
                        salaryService.generatePayslip(request), 201));
    }

    @Operation(summary = "Get all payslips for employee")
    @GetMapping("/payslip/{employeeId}")
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> getEmployeePayslips(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                ApiResponse.success("Payslips fetched",
                        salaryService.getEmployeePayslips(employeeId), 200)
        );
    }

    @Operation(summary = "Get payslip by ID")
    @GetMapping("/payslip/detail/{id}")
    public ResponseEntity<ApiResponse<PayslipResponse>> getPayslipById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Payslip fetched", salaryService.getPayslipById(id), 200)
        );
    }
}
